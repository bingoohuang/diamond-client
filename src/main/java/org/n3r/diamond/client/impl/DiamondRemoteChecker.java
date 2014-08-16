package org.n3r.diamond.client.impl;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.commons.lang3.StringUtils;
import org.n3r.diamond.client.DiamondListener;
import org.n3r.diamond.client.DiamondStone;
import org.n3r.diamond.client.cache.DiamondCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

class DiamondRemoteChecker {
    private final DiamondCache diamondCache;
    private Logger log = LoggerFactory.getLogger(DiamondRemoteChecker.class);

    private Cache<DiamondStone.DiamondAxis, Optional<String>> contentCache = CacheBuilder.newBuilder()
            .expireAfterAccess(15, TimeUnit.MINUTES)
            .maximumSize(10000)
            .build();

    private volatile DiamondAllListener diamondAllListener = new DiamondAllListener();


    private DiamondHttpClient httpClient;
    private final DiamondManagerConf managerConfig;
    private final DiamondSubscriber diamondSubscriber;

    public DiamondRemoteChecker(DiamondSubscriber diamondSubscriber,
                                DiamondManagerConf managerConfig,
                                DiamondCache diamondCache) {
        this.diamondSubscriber = diamondSubscriber;
        this.managerConfig = managerConfig;
        this.diamondCache = diamondCache;

        httpClient = new DiamondHttpClient(managerConfig);
        managerConfig.randomDomainNamePos();
    }

    public void addDiamondListener(DiamondStone.DiamondAxis diamondAxis, DiamondListener diamondListener) {
        diamondAllListener.addDiamondListener(diamondAxis, diamondListener);
    }

    public void removeDiamondListener(DiamondStone.DiamondAxis diamondAxis, DiamondListener diamondListener) {
        diamondAllListener.removeDiamondListener(diamondAxis, diamondListener);
    }


    public void shutdown() {
        httpClient.shutdown();
    }

    public void checkRemote() {
        Set<String> updateDataIdGroupPairs = checkUpdateDataIds(managerConfig.getReceiveWaitTime());
        if (null == updateDataIdGroupPairs || updateDataIdGroupPairs.size() == 0) {
            return;
        }

        for (String freshDataIdGroupPair : updateDataIdGroupPairs) {
            int middleIndex = freshDataIdGroupPair.indexOf(Constants.WORD_SEPARATOR);
            if (middleIndex == -1) continue;

            String freshDataId = freshDataIdGroupPair.substring(0, middleIndex);
            String freshGroup = freshDataIdGroupPair.substring(middleIndex + 1);

            DiamondStone.DiamondAxis diamondAxis = DiamondStone.DiamondAxis.makeAxis(freshGroup, freshDataId);
            DiamondMeta diamondMeta = diamondSubscriber.getCachedMeta(diamondAxis);

            receiveDiamondContent(diamondMeta);
        }
    }

    private void receiveDiamondContent(final DiamondMeta diamondMeta) {
        try {
            retrieveRemoteAndInvokeListeners(diamondMeta);
        } catch (Exception e) {
            log.error("retrieveRemoteAndInvokeListeners error", e.getMessage());
        }
    }

    private void retrieveRemoteAndInvokeListeners(DiamondMeta diamondMeta) {
        String diamondContent = retrieveRemote(diamondMeta.getDiamondAxis(),
                managerConfig.getReceiveWaitTime(), false);
        // if (null == diamondContent) return;

        if (null == diamondAllListener) {
            log.warn("null == configInfoListenable");
            return;
        }

        onDiamondChanged(diamondMeta, diamondContent);
    }


    Future<Object> onDiamondChanged(final DiamondMeta diamondMeta, final String content) {
        final DiamondStone diamondStone = new DiamondStone();
        diamondStone.setContent(content);
        diamondStone.setDiamondAxis(diamondMeta.getDiamondAxis());
        diamondMeta.incSuccCounterAndGet();

        Callable<Object> command = new Callable<Object>() {
            public Object call() {
                try {
                    diamondSubscriber.saveSnapshot(diamondStone.getDiamondAxis(), content);
                    diamondAllListener.accept(diamondStone);
                    return diamondCache.updateDiamondCacheOnChange(diamondStone.getDiamondAxis(), content);
                } catch (Throwable t) {
                    log.error("onDiamondChanged error，{}", diamondMeta.getDiamondAxis(), t);
                }
                return null;
            }
        };

        ExecutorService executor = diamondAllListener.getExecutor();
        if (executor == null) executor = MoreExecutors.sameThreadExecutor();
        return executor.submit(command);
    }

    String retrieveRemote(DiamondStone.DiamondAxis diamondAxis, long timeout, boolean useContentCache) {
        diamondSubscriber.start();

        if (useContentCache) {
            Optional<String> optional = contentCache.getIfPresent(diamondAxis);
            if (optional != null) return optional.orNull();
        }

        long costTime = 0;

        String uri = getUriString(diamondAxis);
        log.info(uri);


        int totalRetryTimes = managerConfig.getRetrieveDataRetryTimes();
        int triedTimes = 0;

        Exception lastException = null; // for reduce logs
        int lastHttpStatus = -1;
        while (0 == timeout || timeout > costTime) {
            if (triedTimes > 0) managerConfig.rotateToNextDomain();

            if (++triedTimes > totalRetryTimes + 1) {
                log.warn("reached the max retry times");
                break;
            }

            log.info("retrieve config，try {} times with costTime {}", triedTimes, costTime);

            long onceTimeOut = getOnceTimeOut(costTime, timeout);
            costTime += onceTimeOut;

            try {
                DiamondMeta diamondMeta = diamondSubscriber.getCachedMeta(diamondAxis);
                DiamondHttpClient.GetDiamondResult getDiamondResult;
                getDiamondResult = httpClient.getDiamond(uri, useContentCache, diamondMeta, onceTimeOut);

                int httpStatus = getDiamondResult.getHttpStatus();
                switch (httpStatus) {
                    case Constants.SC_OK:
                        return onSuccess(diamondAxis, diamondMeta, getDiamondResult);
                    case Constants.SC_NOT_MODIFIED:
                        return onNotModified(diamondAxis, diamondMeta, getDiamondResult);
                    case Constants.SC_NOT_FOUND:
                        log.warn("{} not found", diamondAxis);
                        diamondMeta.setMd5(Constants.NULL);
                        diamondSubscriber.removeSnapshot(diamondAxis);
                        diamondCache.removeCacheSnapshot(diamondAxis);
                        contentCache.put(diamondAxis, Optional.<String>absent());
                        return null;
                    default: {
                        if (httpStatus != lastHttpStatus) {
                            log.warn("{}: HTTP State: {} : {} ", diamondAxis, httpStatus, httpClient.getState());
                            lastHttpStatus = httpStatus;
                        }
                    }
                }
            } catch (Exception e) {
                if (!isMessageSameExeption(e, lastException)) {
                    log.error("{}: http error：{}", diamondAxis, e.getMessage());
                    lastException = e;
                }
            }
        }

        throw new RuntimeException("get config ," + diamondAxis + ", timeout=" + timeout);
    }

    private Set<String> checkUpdateDataIds(long timeout) {
        if (MockDiamondServer.isTestMode()) return null;

        long costTime = 0;

        String probeUpdateString = diamondSubscriber.createProbeUpdateString();
        if (StringUtils.isBlank(probeUpdateString)) return null;

        int lastHttpStatus = -1; // for reduce logs
        Exception lastException = null;
        while (0 == timeout || timeout > costTime) {
            if (costTime > 0)  managerConfig.rotateToNextDomain();

            long onceTimeOut = getOnceTimeOut(costTime, timeout);
            costTime += onceTimeOut;

            try {
                DiamondHttpClient.CheckResult checkResult;
                checkResult = httpClient.checkUpdateDataIds(probeUpdateString, onceTimeOut);
                int httpStatus = checkResult.getHttpStatus();
                switch (httpStatus) {
                    case Constants.SC_OK:
                        return checkResult.getUpdateDataIdsInBody();
                    default:
                        if (httpStatus != lastHttpStatus) {
                            log.warn("get changed DataID list response HTTP State: " + httpStatus);
                            lastHttpStatus = httpStatus;
                        }
                }
            } catch (NoNameServerAvailableException e) {
                log.warn("checkUpdateDataIds error {}", e.getMessage());
                break;
            } catch (Exception e) {
                if (!isMessageSameExeption(e, lastException)){
                    log.warn("checkUpdateDataIds error {}", e.getMessage());
                    lastException = e;
                }
            }
        }
        throw new RuntimeException("get changed dataId list to "
                + managerConfig.getDomainName() + " timeout " + timeout);
    }

    private boolean isMessageSameExeption(Exception e1, Exception e2) {
        if (e1 == e2) return true;
        if (e1 == null) return false;
        if (e2 == null) return false;

        return e1.getMessage().equals(e2.getMessage());
    }


    private String onSuccess(DiamondStone.DiamondAxis diamondAxis, DiamondMeta diamondMeta,
                             DiamondHttpClient.GetDiamondResult httpMethod) {
        String diamondContent = httpMethod.getResponseContent();

        if (!DiamondUtils.checkMd5(diamondContent, httpMethod.getMd5())) {
            throw new RuntimeException("MD5 check error for DataID="
                    + diamondAxis.getDataId() + ", content=" + diamondContent + ", md5=" + httpMethod.getMd5());
        }

        String lastModified = httpMethod.getLastModified();

        diamondMeta.setMd5(httpMethod.getMd5());
        diamondMeta.setLastModifiedHeader(lastModified);

        changeSpacingInterval(httpMethod);

        contentCache.put(diamondAxis, Optional.fromNullable(diamondContent));

        log.debug("received {}, content={}", diamondAxis, diamondContent);

        return diamondContent;
    }

    long getOnceTimeOut(long costTime, long totalTimeout) {
        long onceTimeOut = this.managerConfig.getOnceTimeout();
        long remainTime = totalTimeout - costTime;
        if (onceTimeOut > remainTime) onceTimeOut = remainTime;

        return onceTimeOut;
    }

    private String onNotModified(DiamondStone.DiamondAxis diamondAxis, DiamondMeta diamondMeta,
                                 DiamondHttpClient.GetDiamondResult httpMethod) {
        String md5 = httpMethod.getMd5();
        if (!diamondMeta.getMd5().equals(md5)) {
            String lastMd5 = diamondMeta.getMd5();
            diamondMeta.setMd5(Constants.NULL);
            diamondMeta.setLastModifiedHeader(Constants.NULL);
            throw new RuntimeException("MD5 checked error," + diamondAxis
                    + " last md5=" + lastMd5 + ", current md5=" + md5);
        }

        diamondMeta.setMd5(md5);
        changeSpacingInterval(httpMethod);
        log.info("{} not modified", diamondAxis);
        return null;
    }

    void changeSpacingInterval(DiamondHttpClient.GetDiamondResult httpMethod) {
        int pollingIntervalTime = httpMethod.getPollingInterval();
        if (pollingIntervalTime > 0) managerConfig.setPollingInterval(pollingIntervalTime);
    }

    String getUriString(DiamondStone.DiamondAxis diamondAxis) {
        StringBuilder uriBuilder = new StringBuilder();
        uriBuilder.append(Constants.HTTP_URI_FILE)
                .append("?" + Constants.DATAID + "=" + diamondAxis.getDataId())
                .append("&" + Constants.GROUP + "=" + diamondAxis.getGroup());

        return uriBuilder.toString();
    }
}
