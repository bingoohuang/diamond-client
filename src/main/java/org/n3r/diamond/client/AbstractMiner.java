package org.n3r.diamond.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.MoreObjects;
import org.apache.commons.lang3.StringUtils;
import org.n3r.diamond.client.impl.DiamondUtils;
import org.n3r.diamond.client.impl.PropertiesBasedMiner;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.n3r.diamond.client.impl.DiamondLogger.log;
import static org.n3r.diamond.client.impl.DiamondUtils.parseObject;
import static org.n3r.diamond.client.impl.DiamondUtils.parseObjects;

public abstract class AbstractMiner implements Minerable {
    public abstract String getDefaultGroupName();

    @Override
    public <T> T getObject(String key, Class<T> clazz) {
        return parseObject(getString(key), clazz);
    }

    @Override
    public <T> T getStoneObject(String group, String dataId, Class<T> clazz) {
        return parseObject(getStone(group, dataId), clazz);
    }

    @Override
    public <T> T getObject(String key, String defaultValue, Class<T> clazz) {
        return parseObject(getString(key, defaultValue), clazz);
    }

    @Override
    public <T> T getStoneObject(String group, String dataId, String defaultValue, Class<T> clazz) {
        return parseObject(getStone(group, dataId, defaultValue), clazz);
    }

    @Override
    public <T> List<T> getObjects(String key, Class<T> clazz) {
        return parseObjects(getString(key), clazz);
    }

    @Override
    public <T> List<T> getStoneObjects(String group, String dataId, Class<T> clazz) {
        return parseObjects(getStone(group, dataId), clazz);
    }

    @Override
    public <T> List<T> getObjects(String key, String defaultValue, Class<T> clazz) {
        return parseObjects(getString(key, defaultValue), clazz);
    }

    @Override
    public <T> List<T> getStoneObjects(String group, String dataId, String defaultValue, Class<T> clazz) {
        return parseObjects(getStone(group, dataId, defaultValue), clazz);
    }

    @Override
    public long getBytes(String key) {
        return getBytes(getDefaultGroupName(), key);
    }

    @Override
    public long getBytes(String group, String dataId) {
        String stone = getStone(group, dataId);
        return BytesSize.parseBytes(stone);
    }

    @Override
    public long getBytes(String key, long defaultValue) {
        return getBytes(getDefaultGroupName(), key, defaultValue);
    }

    @Override
    public long getBytes(String group, String dataId, long defaultValue) {
        String stone = getStone(group, dataId);

        return StringUtils.isBlank(stone) ? defaultValue
                : BytesSize.parseBytes(stone);
    }

    @Override
    public long getDuration(String key, TimeUnit timeUnit) {
        return getDuration(getDefaultGroupName(), key, timeUnit);
    }

    @Override
    public long getDuration(String group, String dataId, TimeUnit timeUnit) {
        String stone = getStone(group, dataId);

        return Duration.getDuration(stone, timeUnit);
    }

    @Override
    public long getDuration(String key, TimeUnit timeUnit, long defaultValue) {
        return getDuration(getDefaultGroupName(), key, timeUnit, defaultValue);
    }

    @Override
    public long getDuration(String group, String dataId, TimeUnit timeUnit, long defaultValue) {
        String stone = getStone(group, dataId);
        return StringUtils.isBlank(stone) ? defaultValue
                : Duration.getDuration(stone, timeUnit);
    }

    @Override
    public JSONObject getJSON(String key) {
        return getJSON(getDefaultGroupName(), key);
    }

    @Override
    public <T> T getJSON(String key, Class<T> clazz) {
        return getJSON(getDefaultGroupName(), key, clazz);
    }

    @Override
    public JSONObject getJSON(String group, String dataId) {
        String stone = getStone(group, dataId);
        if (stone == null) return null;
        try {
            return JSON.parseObject(stone);
        } catch (Exception e) {
            log().error("parse stone to JSON failed " + stone, e);
            throw new DiamondException.WrongType(e);
        }
    }

    @Override
    public <T> T getJSON(String group, String dataId, Class<T> clazz) {
        String stone = getStone(group, dataId);
        if (stone == null) return null;
        try {
            return JSON.parseObject(stone, clazz);
        } catch (Exception e) {
            log().error("parse stone to JSON failed " + stone, e);
            throw new DiamondException.WrongType(e);
        }
    }

    @Override
    public JSONArray getJSONArray(String key) {
        return getJSONArray(getDefaultGroupName(), key);
    }

    @Override
    public <T> List<T> getJSONArray(String key, Class<T> clazz) {
        return getJSONArray(getDefaultGroupName(), key, clazz);
    }

    @Override
    public JSONArray getJSONArray(String group, String dataId) {
        String stone = getStone(group, dataId);
        if (stone == null) return null;
        try {
            return JSON.parseArray(stone);
        } catch (Exception e) {
            log().error("parse stone to JSONArray failed " + stone, e);
            throw new DiamondException.WrongType(e);
        }
    }

    @Override
    public <T> List<T> getJSONArray(String group, String dataId, Class<T> clazz) {
        String stone = getStone(group, dataId);
        if (stone == null) return null;
        try {
            return JSON.parseArray(stone, clazz);
        } catch (Exception e) {
            log().error("parse stone to JSONArray failed " + stone, e);
            throw new DiamondException.WrongType(e);
        }
    }

    @Override
    public Properties getProperties(String key) {
        return getProperties(getDefaultGroupName(), key);
    }

    @Override
    public Properties getProperties(String group, String dataId) {
        String stone = getStone(group, dataId);
        return DiamondUtils.parseStoneToProperties(stone);
    }

    @Override
    public Minerable getMiner(String key) {
        return getMiner(getDefaultGroupName(), key);
    }

    @Override
    public Minerable getMiner(String group, String dataId) {
        return new PropertiesBasedMiner(getProperties(group, dataId));
    }

    @Override
    public String getString(String key) {
        return getStone(getDefaultGroupName(), key);
    }

    @Override
    public String getString(String key, String defaultValue) {
        return getStone(getDefaultGroupName(), key, defaultValue);
    }

    @Override
    public String getStone(String group, String dataId, String defaultValue) {
        return MoreObjects.firstNonNull(getStone(group, dataId), defaultValue);
    }

    @Override
    public boolean exists(String group, String dataId) {
        String stone = getStone(group, dataId);
        return StringUtils.isNotBlank(stone);
    }

    @Override
    public boolean exists(String key) {
        return exists(getDefaultGroupName(), key);
    }

    @Override
    public int getInt(String key) {
        return getInt(getDefaultGroupName(), key);
    }

    @Override
    public int getInt(String group, String dataId) {
        String stone = getStoneAndCheckMissing(group, dataId);

        try {
            return Integer.parseInt(stone);
        } catch (NumberFormatException e) {
            throw new DiamondException.WrongType(e);
        }
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return getInt(getDefaultGroupName(), key, defaultValue);
    }

    @Override
    public int getInt(String group, String dataId, int defaultValue) {
        String stone = getStone(group, dataId);
        if (stone == null) return defaultValue;

        try {
            return Integer.parseInt(stone);
        } catch (NumberFormatException e) {
            throw new DiamondException.WrongType(e);
        }
    }

    @Override
    public long getLong(String key) {
        return getLong(getDefaultGroupName(), key);
    }

    @Override
    public long getLong(String group, String dataId) {
        String stone = getStoneAndCheckMissing(group, dataId);

        try {
            return Long.parseLong(stone);
        } catch (NumberFormatException e) {
            throw new DiamondException.WrongType(e);
        }
    }

    @Override
    public long getLong(String key, long defaultValue) {
        return getLong(getDefaultGroupName(), key, defaultValue);
    }

    @Override
    public long getLong(String group, String dataId, long defaultValue) {
        String stone = getStone(group, dataId);
        if (stone == null) return defaultValue;

        try {
            return Long.parseLong(stone);
        } catch (NumberFormatException e) {
            throw new DiamondException.WrongType(e);
        }
    }

    @Override
    public boolean getBool(String key) {
        return getBool(getDefaultGroupName(), key);
    }


    @Override
    public boolean getBool(String group, String dataId) {
        String stone = getStoneAndCheckMissing(group, dataId);

        return DiamondUtils.toBool(stone);
    }

    @Override
    public boolean getBool(String key, boolean defaultValue) {
        return getBool(getDefaultGroupName(), key, defaultValue);
    }

    @Override
    public boolean getBool(String group, String dataId, boolean defaultValue) {
        String stone = getStone(group, dataId);
        if (stone == null) return defaultValue;
        return DiamondUtils.toBool(stone);
    }

    @Override
    public float getFloat(String key) {
        return getFloat(getDefaultGroupName(), key);
    }

    @Override
    public float getFloat(String group, String dataId) {
        String stone = getStoneAndCheckMissing(group, dataId);
        try {
            return Float.parseFloat(stone);
        } catch (NumberFormatException e) {
            throw new DiamondException.WrongType(e);
        }
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        return getFloat(getDefaultGroupName(), key, defaultValue);
    }

    @Override
    public float getFloat(String group, String dataId, float defaultValue) {
        String stone = getStone(group, dataId);
        if (stone == null) return defaultValue;

        try {
            return Float.parseFloat(stone);
        } catch (NumberFormatException e) {
            throw new DiamondException.WrongType(e);
        }
    }

    @Override
    public double getDouble(String key) {
        return getDouble(getDefaultGroupName(), key);
    }

    @Override
    public double getDouble(String group, String dataId) {
        String stone = getStoneAndCheckMissing(group, dataId);

        try {
            return Double.parseDouble(stone);
        } catch (NumberFormatException e) {
            throw new DiamondException.WrongType(e);
        }
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        return getDouble(getDefaultGroupName(), key, defaultValue);
    }

    @Override
    public double getDouble(String group, String dataId, double defaultValue) {
        String stone = getStone(group, dataId);
        if (stone == null) return defaultValue;

        try {
            return Double.parseDouble(stone);
        } catch (NumberFormatException e) {
            throw new DiamondException.WrongType(e);
        }
    }

    private String getStoneAndCheckMissing(String group, String dataId) {
        String stone = getStone(group, dataId);
        if (stone == null) throw new DiamondException.Missing();
        return stone;
    }

}
