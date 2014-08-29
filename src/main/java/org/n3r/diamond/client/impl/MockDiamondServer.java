package org.n3r.diamond.client.impl;

import org.n3r.diamond.client.DiamondAxis;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;


public class MockDiamondServer {
    static ConcurrentHashMap<DiamondAxis, String>
            mocks = new ConcurrentHashMap<DiamondAxis, String>();
    static volatile boolean testMode = false;

    public static void setUpMockServer() {
        testMode = true;
    }

    public static void tearDownMockServer() {
        mocks.clear();
//        DiamondSubscriber.getInstance().close();
        testMode = false;
    }

    public static Future<Object> updateDiamond(String dataId, String configInfo) {
        return updateDiamond(Constants.DEFAULT_GROUP, dataId, configInfo);
    }

    public static Future<Object> updateDiamond(String group, String dataId, String configInfo) {
        DiamondSubscriber diamondSubscriber = DiamondSubscriber.getInstance();
        DiamondRemoteChecker remoteChecker = diamondSubscriber.getDiamondRemoteChecker();
        DiamondAxis diamondAxis = DiamondAxis.makeAxis(group, dataId);
        return remoteChecker.onDiamondChanged(diamondSubscriber.getCachedMeta(diamondAxis), configInfo);
    }

    public static String getDiamond(DiamondAxis diamondAxis) {
        return mocks.get(diamondAxis);
    }


    public static void setConfigInfos(Map<String, String> configInfos) {
        if (null == configInfos) return;

        for (Map.Entry<String, String> entry : configInfos.entrySet()) {
            setConfigInfo(entry.getKey(), entry.getValue());
        }
    }

    public static void setConfigInfo(String dataId, String configInfo) {
        setConfigInfo(DiamondAxis.makeAxis(dataId), configInfo);
    }

    public static void setConfigInfo(String group, String dataId, String configInfo) {
        setConfigInfo(DiamondAxis.makeAxis(group, dataId), configInfo);
    }

    private static void setConfigInfo(DiamondAxis diamondAxis, String configInfo) {
        mocks.put(diamondAxis, configInfo);
    }

    public static boolean isTestMode() {
        return testMode;
    }
}