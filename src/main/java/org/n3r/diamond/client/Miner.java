package org.n3r.diamond.client;

import org.n3r.diamond.client.impl.Constants;
import org.n3r.diamond.client.impl.DiamondSubstituter;

@SuppressWarnings("unchecked")
public class Miner extends AbstractMiner implements CacheMinerable {
    private final String defaultGroupName;

    public Miner() {
        this(Constants.DEFAULT_GROUP);
    }

    public Miner(String defaultGroupName) {
        this.defaultGroupName = defaultGroupName;
    }

    @Override
    public String getStone(String group, String dataId) {
        String diamond = new DiamondManager(group, dataId).getDiamond();
        if (diamond == null) return null;

        return DiamondSubstituter.substitute(diamond, true, group, dataId, null);
    }

    @Override
    public <T> T getCache(String key) {
        return getCache(getDefaultGroupName(), key);
    }


    @Override
    public <T> T getDynamicCache(String key, Object... dynamics) {
        return getDynamicStoneCache(getDefaultGroupName(), key, dynamics);
    }

    @Override
    public <T> T getDynamicStoneCache(String group, String dataId, Object... dynamics) {
        return (T) new DiamondManager(group, dataId).getDynamicCache(dynamics);
    }

    @Override
    public <T> T getCache(String group, String dataId) {
        return (T) new DiamondManager(group, dataId).getCache();
    }

    @Override
    public String getDefaultGroupName() {
        return defaultGroupName;
    }
}
