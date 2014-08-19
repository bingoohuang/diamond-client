package org.n3r.diamond.client;


public interface CacheMinerable {
    <T> T getCache(String key);
    <T> T getCache(String group, String dataId);
    <T> T getDynamicCache(String key, Object... dynamics);
    <T> T getDynamicStoneCache(String group, String dataId, Object... dynamics);
}
