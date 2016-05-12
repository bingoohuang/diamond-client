package org.n3r.diamond.client.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang3.StringUtils;
import org.n3r.diamond.client.DiamondAxis;
import org.n3r.diamond.client.DiamondListener;
import org.n3r.diamond.client.cache.DiamondCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DiamondSubscriber implements Closeable {
    private static DiamondSubscriber instance = new DiamondSubscriber();

    public static DiamondSubscriber getInstance() {
        return instance;
    }

    private Logger log = LoggerFactory.getLogger(DiamondSubscriber.class);

    private final LoadingCache<DiamondAxis, DiamondMeta> metaCache
            = CacheBuilder.newBuilder()
            .build(new CacheLoader<DiamondAxis, DiamondMeta>() {
                @Override
                public DiamondMeta load(DiamondAxis key) throws Exception {
                    start();
                    return new DiamondMeta(key);
                }
            });

    private volatile DiamondManagerConf managerConfig = new DiamondManagerConf();

    private ScheduledExecutorService scheduler;
    private LocalDiamondMiner localDiamondMiner = new LocalDiamondMiner();
    private ServerAddressesMiner serverAddressesMiner;
    private SnapshotMiner snapshotMiner;
    private DiamondCache diamondCache;

    private volatile boolean running;

    private DiamondRemoteChecker diamondRemoteChecker;

    private DiamondSubscriber() {
    }

    public void addDiamondListener(DiamondAxis diamondAxis, DiamondListener diamondListener) {
        diamondRemoteChecker.addDiamondListener(diamondAxis, diamondListener);
    }

    public void removeDiamondListener(DiamondAxis diamondAxis, DiamondListener diamondListener) {
        diamondRemoteChecker.removeDiamondListener(diamondAxis, diamondListener);
    }

    public synchronized void start() {
        if (running) return;

        if (null == scheduler || scheduler.isTerminated())
            scheduler = Executors.newSingleThreadScheduledExecutor();

        localDiamondMiner.start(managerConfig);

        snapshotMiner = new SnapshotMiner(managerConfig);
        diamondCache = new DiamondCache(snapshotMiner);

        if (!ClientProperties.isPureLocalMode() || MockDiamondServer.isTestMode()) {
            DiamondHttpClient diamondHttpClient = new DiamondHttpClient(managerConfig);

            serverAddressesMiner = new ServerAddressesMiner(
                    managerConfig, scheduler, diamondHttpClient);
            serverAddressesMiner.start();

            diamondRemoteChecker = new DiamondRemoteChecker(this,
                    managerConfig, diamondCache, diamondHttpClient);

            log.info("diamond servers {}", managerConfig.getDiamondServers());

            rotateCheckDiamonds();
        }

        running = true;

        addShutdownHook();
    }


    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                close();
            }
        });
    }

    private void rotateCheckDiamonds() {
        int pollingInterval = managerConfig.getPollingInterval();
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                new DiamondExtenderManager().loadDiamondExtenders();
            }
        }, 5, TimeUnit.SECONDS);

        scheduler.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                rotateCheckDiamondsTask();
            }

        }, pollingInterval, pollingInterval, TimeUnit.SECONDS);
    }

    private void rotateCheckDiamondsTask() {
        try {
            checkLocal();
            diamondRemoteChecker.checkRemote();
            checkSnapshot();
        } catch (Exception e) {
            log.warn("rotateCheckDiamondsTask error {}", e.getMessage());
        }
    }


    @Override
    public synchronized void close() {
        if (!running) return;
        running = false;

        log.warn("start to close DiamondSubscriber");

        localDiamondMiner.stop();
        serverAddressesMiner.stop();

        scheduler.shutdownNow();

//        metaCache.invalidateAll();
        diamondRemoteChecker.shutdown();
        diamondCache.close();

        log.warn("end to close DiamondSubscriber");
    }

    public DiamondRemoteChecker getDiamondRemoteChecker() {
        return diamondRemoteChecker;
    }

    public String retrieveDiamondLocalAndRemote(DiamondAxis diamondAxis, long timeout) {
        DiamondMeta diamondMeta = getCachedMeta(diamondAxis);
        // local first
        try {
            String localConfig = localDiamondMiner.readLocal(diamondMeta);
            if (localConfig != null) {
                diamondMeta.incSuccCounterAndGet();
                saveSnapshot(diamondAxis, localConfig);
                return localConfig;
            }
        } catch (Exception e) {
            log.error("get local error", e);
        }

        String result = diamondRemoteChecker.retrieveRemote(diamondAxis, timeout, true);
        if (result != null) {
            saveSnapshot(diamondAxis, result);
            diamondMeta.incSuccCounterAndGet();
        }

        return result;
    }

    public void saveSnapshot(DiamondAxis diamondAxis, String diamondContent) {
        snapshotMiner.saveSnaptshot(diamondAxis, diamondContent);
    }

    public String getDiamond(DiamondAxis diamondAxis, long timeout) {
        if (MockDiamondServer.isTestMode())
            return MockDiamondServer.getDiamond(diamondAxis);

        try {
            String result = retrieveDiamondLocalAndRemote(diamondAxis, timeout);
            if (StringUtils.isNotBlank(result)) return result;
        } catch (Exception t) {
            log.error(t.getMessage());
        }

        if (MockDiamondServer.isTestMode()) return null;

        return getSnapshot(diamondAxis);
    }


    public String getSnapshot(DiamondAxis diamondAxis) {
        try {
            DiamondMeta diamondMeta = getCachedMeta(diamondAxis);
            String diamondContent = snapshotMiner.getSnapshot(diamondAxis);
            if (diamondContent != null && diamondMeta != null)
                diamondMeta.incSuccCounterAndGet();

            return diamondContent;
        } catch (Exception e) {
            log.error("getSnapshot diamondAxis {} error {}", diamondAxis, e.getMessage());
            return null;
        }
    }

    public void removeSnapshot(DiamondAxis diamondAxis) {
        snapshotMiner.removeSnapshot(diamondAxis);
    }

    public void checkSnapshot() {
        for (Map.Entry<DiamondAxis, DiamondMeta> entry : metaCache.asMap().entrySet()) {
            final DiamondMeta diamondMeta = entry.getValue();

            if (diamondMeta.isUseLocal()) continue;
            if (diamondMeta.getFetchCount() > 0) continue;

            String diamond = getSnapshot(diamondMeta.getDiamondAxis());
            if (diamond != null)
                diamondRemoteChecker.onDiamondChanged(diamondMeta, diamond);
        }
    }

    public DiamondMeta getCachedMeta(DiamondAxis diamondAxis) {
        return metaCache.getUnchecked(diamondAxis);
    }

    public void checkLocal() {
        for (Map.Entry<DiamondAxis, DiamondMeta> entry : metaCache.asMap().entrySet()) {
            final DiamondMeta diamondMeta = entry.getValue();

            try {
                String content = localDiamondMiner.checkLocal(diamondMeta);
                if (null != content) {
                    log.info("local config read, {}", diamondMeta.getDiamondAxis());

                    diamondRemoteChecker.onDiamondChanged(diamondMeta, content);
                }
            } catch (Exception e) {
                log.error("check local error", e);
            }
        }
    }


    public String createProbeUpdateString() {
        StringBuilder probeModifyBuilder = new StringBuilder();
        for (Map.Entry<DiamondAxis, DiamondMeta> entry : metaCache.asMap().entrySet()) {
            final DiamondMeta data = entry.getValue();
            if (data.isUseLocal()) continue;

            DiamondAxis axis = data.getDiamondAxis();

            probeModifyBuilder.append(axis.getDataId())
                    .append(Constants.WORD_SEPARATOR).append(axis.getGroup())
                    .append(Constants.WORD_SEPARATOR).append(data.getMd5())
                    .append(Constants.LINE_SEPARATOR);
        }

        return probeModifyBuilder.toString();
    }

    public Object getCache(DiamondAxis diamondAxis, int timeoutMillis, Object... dynamics) {
        String diamondContent = getDiamond(diamondAxis, timeoutMillis);
        if (diamondContent == null) return null;

        return diamondCache.getCache(diamondAxis, diamondContent, dynamics);
    }
}
