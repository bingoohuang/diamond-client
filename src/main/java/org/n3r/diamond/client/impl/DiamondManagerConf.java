package org.n3r.diamond.client.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static org.n3r.diamond.client.impl.Constants.*;

public class DiamondManagerConf {
    private Logger log = LoggerFactory.getLogger(DiamondManagerConf.class);

    private volatile int pollingInterval = POLLING_INTERVAL; // interval for periodically check
    private volatile int onceTimeout = ONCE_TIMEOUT; // Timeout for one try config from diamond-server
    private volatile int receiveWaitTime = RECV_WAIT_TIMEOUT; // total timeout for one config with multi tries

    private AtomicInteger domainNamePos = new AtomicInteger(0);

    private volatile List<String> diamondServers = new LinkedList<String>();

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

    public void setDiamondServers(List<String> diamondServers) {
        this.diamondServers = new LinkedList<String>(diamondServers);
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
        if (diamondServers.size()  == 0)
            throw new NoNameServerAvailableException("no name server available!");

        return diamondServers.get(domainNamePos.get());
    }

    public void randomDomainNamePos() {
        if (!diamondServers.isEmpty()) {
            domainNamePos.set(new Random().nextInt(diamondServers.size()));
            log.info("random DiamondServer to：" + getDomainName());
        }
    }

    synchronized void rotateToNextDomain() {
        int index = domainNamePos.incrementAndGet();
        if (index < 0) index = -index;

        int domainNameCount = diamondServers.size();
        if (domainNameCount == 0) {
            log.error("diamond server list is empty, please contact administrator");
            return;
        }

        if (diamondServers.size() > 0) {
            domainNamePos.set(index % domainNameCount);
            log.warn("rotate diamond server to " + getDomainName());
        }
    }

}
