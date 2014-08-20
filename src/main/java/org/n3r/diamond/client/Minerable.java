package org.n3r.diamond.client;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public interface Minerable {
    long getBytes(String key);
    long getBytes(String group, String dataId);
    long getBytes(String key, long defaultValue);
    long getBytes(String group, String dataId, long defaultValue);

    long getDuration(String key, TimeUnit timeUnit);
    long getDuration(String group, String key, TimeUnit timeUnit);
    long getDuration(String key, TimeUnit timeUnit, long defaultValue);
    long getDuration(String group, String key, TimeUnit timeUnit, long defaultValue);

    JSONObject getJSON(String key);
    <T> T getJSON(String key, Class<T> clazz);
    JSONObject getJSON(String group, String dataId);
    <T> T getJSON(String group, String dataId, Class<T> clazz);

    JSONArray getJSONArray(String key);
    <T> List<T> getJSONArray(String key, Class<T> clazz);
    JSONArray getJSONArray(String group, String dataId);
    <T> List<T> getJSONArray(String group, String dataId, Class<T> clazz);

    Properties getProperties(String key);
    Properties getProperties(String group, String dataId);

    Minerable getMiner(String key);
    Minerable getMiner(String group, String dataId);

    String getString(String key);
    String getString(String key, String defaultValue);
    String getStone(String group, String dataId);
    String getStone(String group, String dataId, String defaultValue);

    <T> T getObject(String key, Class<T> clazz);
    <T> T getStoneObject(String group, String dataId, Class<T> clazz);
    <T> T getObject(String key, String defaultValue , Class<T> clazz);
    <T> T getStoneObject(String group, String dataId, String defaultValue, Class<T> clazz);

    <T> List<T> getObjects(String key, Class<T> clazz);
    <T> List<T> getStoneObjects(String group, String dataId, Class<T> clazz);
    <T> List<T> getObjects(String key, String defaultValue , Class<T> clazz);
    <T> List<T> getStoneObjects(String group, String dataId, String defaultValue, Class<T> clazz);

    boolean exists(String group, String dataId);
    boolean exists(String key);

    int getInt(String key);
    int getInt(String group, String dataId);
    int getInt(String key, int defaultValue);
    int getInt(String group, String dataId, int defaultValue);

    long getLong(String key);
    long getLong(String group, String dataId);
    long getLong(String key, long defaultValue);
    long getLong(String group, String dataId, long defaultValue);

    boolean getBool(String key);
    boolean getBool(String group, String dataId);
    boolean getBool(String key, boolean defaultValue);
    boolean getBool(String group, String dataId, boolean defaultValue);

    float getFloat(String key);
    float getFloat(String group, String dataId);
    float getFloat(String key, float defaultValue);
    float getFloat(String group, String dataId, float defaultValue);

    double getDouble(String key);
    double getDouble(String group, String dataId);
    double getDouble(String key, double defaultValue);
    double getDouble(String group, String dataId, double defaultValue);
}
