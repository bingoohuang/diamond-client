package org.n3r.diamond.client.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.n3r.diamond.client.impl.Constants.*;

@Slf4j
public class DiamondManagerConf {
    private volatile int pollingInterval = POLLING_INTERVAL; // interval for periodically check
    private volatile int onceTimeout = ONCE_TIMEOUT; // Timeout for one try config from diamond-server
    private volatile int receiveWaitTime = RECV_WAIT_TIMEOUT; // total timeout for one config with multi tries

    private AtomicInteger domainNamePos = new AtomicInteger(0);

    private volatile List<String> diamondServers = Lists.newArrayList();

    private int maxHostConnections = 1;
    private boolean connectionStaleCheckingEnabled = true;
    private int maxTotalConnections = 20;
    private int connectionTimeout = CONN_TIMEOUT;
    private int retrieveDataRetryTimes = Integer.MAX_VALUE / 10;

    private String filePath; // local data dir root

    public DiamondManagerConf() {
        filePath = System.getProperty("user.home") + File.separator + ".diamond-client";
        File dir = new File(filePath);
        dir.mkdirs();
        if (!dir.exists()) throw new RuntimeException("create diamond-miner dir fail " + filePath);
    }


    public int getMaxHostConnections() {
        return maxHostConnections;
    }

    public void setMaxHostConnections(int maxHostConnections) {
        this.maxHostConnections = maxHostConnections;
    }

    public boolean isConnectionStaleCheckingEnabled() {
        return connectionStaleCheckingEnabled;
    }

    public void setConnectionStaleCheckingEnabled(boolean connectionStaleCheckingEnabled) {
        this.connectionStaleCheckingEnabled = connectionStaleCheckingEnabled;
    }

    public int getMaxTotalConnections() {
        return maxTotalConnections;
    }

    public void setMaxTotalConnections(int maxTotalConnections) {
        this.maxTotalConnections = maxTotalConnections;
    }

    public int getPollingInterval() {
        return pollingInterval;
    }


    public void setPollingInterval(int pollingInterval) {
        if (pollingInterval < POLLING_INTERVAL && !MockDiamondServer.isTestMode()) return;
        this.pollingInterval = pollingInterval;
    }


    public List<String> getDiamondServers() {
        return diamondServers;
    }

    public boolean hasDiamondServers() {
        return diamondServers.size() > 0;
    }

    public void setDiamondServers(Set<String> diamondServers, DiamondHttpClient diamondHttpClient) {
        if (Sets.newHashSet(this.diamondServers).equals(diamondServers)) return;

        this.diamondServers = Lists.newArrayList(diamondServers);
        randomDomainNamePos();
        diamondHttpClient.resetHostConfig(getDomainName());
    }

    public void addDomainName(String domainName) {
        this.diamondServers.add(domainName);
    }

    public String getFilePath() {
        return filePath;
    }

    public int getOnceTimeout() {
        return onceTimeout;
    }

    public void setOnceTimeout(int onceTimeout) {
        this.onceTimeout = onceTimeout;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }


    public int getReceiveWaitTime() {
        return receiveWaitTime;
    }

    public void setReceiveWaitTime(int receiveWaitTime) {
        this.receiveWaitTime = receiveWaitTime;
    }

    public int getRetrieveDataRetryTimes() {
        return retrieveDataRetryTimes;
    }

    public void setRetrieveDataRetryTimes(int retrieveDataRetryTimes) {
        this.retrieveDataRetryTimes = retrieveDataRetryTimes;
    }

    public String getDomainName() {
        if (diamondServers.size() == 0)
            throw new NoNameServerAvailableException("no name server available!");

        return diamondServers.get(domainNamePos.get());
    }

    private void randomDomainNamePos() {
        int diamondServerNum = diamondServers.size();
        if (diamondServerNum > 1) {
            domainNamePos.set(new Random().nextInt(diamondServerNum));
            log.info("random DiamondServer to：" + getDomainName());
        }
    }

    synchronized void rotateToNextDomain(DiamondHttpClient diamondHttpClient) {
        int diamondServerNum = diamondServers.size();
        if (diamondServerNum == 0) {
            log.error("diamond server list is empty, please contact administrator");
            return;
        }

        if (diamondServerNum <= 1) {
            diamondHttpClient.resetHostConfig(getDomainName());
            return;
        }

        int index = domainNamePos.incrementAndGet();
        if (index < 0) index = -index;
        domainNamePos.set(index % diamondServerNum);

        diamondHttpClient.resetHostConfig(getDomainName());

        log.warn("rotate diamond server to " + getDomainName());
    }

}
