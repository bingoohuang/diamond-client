package org.n3r.diamond.client;

import org.n3r.diamond.client.impl.Constants;
import org.n3r.diamond.client.impl.DiamondSubscriber;
import org.n3r.diamond.client.impl.DiamondUtils;

public class DiamondManager {
    private DiamondSubscriber diamondSubscriber = DiamondSubscriber.getInstance();

    private final DiamondAxis diamondAxis;
    private int timeoutMillis = 3000;  // timeout in millis of network

    public DiamondManager(String dataId) {
        this(Constants.DEFAULT_GROUP, dataId);
    }

    public DiamondManager(String group, String dataId) {
        diamondAxis = DiamondAxis.makeAxis(group, dataId);

        diamondSubscriber.getCachedMeta(diamondAxis);
    }

    public void addDiamondListener(DiamondListener diamondListener) {
        diamondSubscriber.addDiamondListener(diamondAxis, diamondListener);
    }

    public void removeDiamondListener(DiamondListener diamondListener) {
        diamondSubscriber.removeDiamondListener(diamondAxis, diamondListener);
    }

    public String getDiamond() {
        String original = diamondSubscriber.getDiamond(diamondAxis, timeoutMillis);
        return DiamondUtils.tryDecrypt(original, diamondAxis.getDataId());
    }

    public Object getCache() {
        return diamondSubscriber.getCache(diamondAxis, timeoutMillis);
    }

    public Object getDynamicCache(Object... dynamics) {
        return diamondSubscriber.getCache(diamondAxis, timeoutMillis, dynamics);
    }

    public void setTimeoutMillis(int timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }


}
