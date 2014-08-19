package org.n3r.diamond.client;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Properties;

public class DiamondMiner {
    static Miner miner = new Miner();

    public static long getBytes(String key) {
        return miner.getBytes(key);
    }

    public static long getBytes(String group, String dataId) {
        return miner.getBytes(group, dataId);
    }

    public static JSONObject getJSON(String key) {
        return miner.getJSON(key);
    }

    public static <T> T getJSON(String key, Class<T> clazz) {
        return miner.getJSON(key, clazz);
    }

    public static JSONObject getJSON(String group, String dataId) {
        return miner.getJSON(group, dataId);
    }

    public static <T> T getJSON(String group, String dataId, Class<T> clazz) {
        return miner.getJSON(group, dataId, clazz);
    }

    public static JSONArray getJSONArray(String key) {
        return miner.getJSONArray(key);
    }

    public static <T> List<T> getJSONArray(String key, Class<T> clazz) {
        return miner.getJSONArray(key, clazz);
    }

    public static JSONArray getJSONArray(String group, String dataId) {
        return miner.getJSONArray(group, dataId);
    }

    public static <T> List<T> getJSONArray(String group, String dataId, Class<T> clazz) {
        return miner.getJSONArray(group, dataId, clazz);
    }


    public static Properties getProperties(String key) {
        return miner.getProperties(key);
    }


    public static Properties getProperties(String group, String dataId) {
        return miner.getProperties(group, dataId);
    }


    public static <T> T getCache(String key) {
        return miner.getCache(key);
    }

    public static <T> T getCache(String group, String dataId) {
        return miner.getCache(group, dataId);
    }

    public static <T> T getDynamicCache(String key, Object... dynamics) {
        return miner.getDynamicCache(key, dynamics);
    }

    public static <T> T getDynamicStoneCache(String group, String dataId, Object... dynamics) {
        return miner.getDynamicStoneCache(group, dataId, dynamics);
    }

    public static String getString(String key) {
        return miner.getString(key);
    }

    public static String getString(String key, String defaultValue) {
        return miner.getString(key, defaultValue);
    }

    public static String getStone(String group, String dataId) {
        return miner.getStone(group, dataId);
    }

    public static String getStone(String group, String dataId, String defaultValue) {
        return miner.getStone(group, dataId, defaultValue);
    }

    public static boolean exists(String group, String dataId) {
        return miner.exists(group, dataId);
    }

    public static boolean exists(String key) {
        return miner.exists(key);
    }

    public static int getInt(String key) {
        return miner.getInt(key);
    }

    public static int getInt(String group, String dataId) {
        return miner.getInt(group, dataId);
    }

    public static int getInt(String key, int defaultValue) {
        return miner.getInt(key, defaultValue);
    }

    public static int getInt(String group, String dataId, int defaultValue) {
        return miner.getInt(group, dataId, defaultValue);
    }

    public static long getLong(String key) {
        return miner.getLong(key);
    }

    public static long getLong(String group, String dataId) {
        return miner.getLong(group, dataId);
    }

    public static long getLong(String key, long defaultValue) {
        return miner.getLong(key, defaultValue);
    }

    public static long getLong(String group, String dataId, long defaultValue) {
        return miner.getLong(group, dataId, defaultValue);
    }

    public static boolean getBoolean(String key) {
        return miner.getBool(key);
    }

    public static boolean getBoolean(String group, String dataId) {
        return miner.getBool(group, dataId);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return miner.getBool(key, defaultValue);
    }

    public static boolean getBoolean(String group, String dataId, boolean defaultValue) {
        return miner.getBool(group, dataId, defaultValue);
    }


    public static float getFloat(String key) {
        return miner.getFloat(key);
    }

    public static float getFloat(String group, String dataId) {
        return miner.getFloat(group, dataId);
    }

    public static float getFloat(String key, float defaultValue) {
        return miner.getFloat(key, defaultValue);
    }

    public static float getFloat(String group, String dataId, float defaultValue) {
        return miner.getFloat(group, dataId, defaultValue);
    }

    public static double getDouble(String key) {
        return miner.getDouble(key);
    }

    public static double getDouble(String group, String dataId) {
        return miner.getDouble(group, dataId);
    }

    public static double getDouble(String key, double defaultValue) {
        return miner.getDouble(key, defaultValue);
    }

    public static double getDouble(String group, String dataId, double defaultValue) {
        return miner.getDouble(group, dataId, defaultValue);
    }

}
