package io.kurumi.ntt.db;

import cn.hutool.core.io.*;
import cn.hutool.json.*;
import io.kurumi.ntt.*;

import java.io.*;
import java.util.*;

public class LocalData {

    private static HashMap<String, String> cache = new HashMap<>();
    private static HashMap<String, JSONObject> json = new HashMap<>();
    private static HashMap<String, JSONArray> jsonArray = new HashMap<>();

    private static String cacheKey(String path, String key) {

        return path + "_" + key;

    }

    private static File cacheFile(String path, String key) {

        return new File("cache".equals(path) ? Env.CACHE_DIR : new File(Env.DATA_DIR, path), key);

    }

    public static boolean exists(String path, String key) {

        return cache.containsKey(cacheKey(path, key)) || cacheFile(path, key).exists();

    }

    public static String gNC(String path, String key) {

        try {

            return FileUtil.readUtf8String(cacheFile(path, key));

        } catch (IORuntimeException e) {
        }

        return null;

    }

    public static String get(String path, String key) {

        String cacheKey = cacheKey(path, key);

        if (cache.containsKey(cacheKey)) return cache.get(cacheKey);

        String value = gNC(path, key);

        if (value == null) return null;

        cache.put(cacheKey, value);

        return value;

    }

    public static void sNC(String path, String key, String value) {

        if (value == null) {

            FileUtil.del(cacheFile(path, key));

        } else {

            FileUtil.writeUtf8String(value, cacheFile(path, key));

        }

    }

    public static void set(String path, String key, String value) {

        sNC(path, key, value);

        String cacheKey = cacheKey(path, key);

        if (value == null) {

            cache.remove(cacheKey);

        } else {

            cache.put(cacheKey, value);

        }

    }

    public static JSONObject getJSON(String path, String key, boolean fix) {

        String cacheKey = cacheKey(path, key);

        if (json.containsKey(cacheKey)) return json.get(cacheKey);

        String value = gNC(path, key + ".json");

        if (value == null) return fix ? new JSONObject() : null;

        try {

            return new JSONObject(value);

        } catch (JSONException err) {

            return fix ? new JSONObject() : null;


        }

    }

    public static void setJSON(String path, String key, JSONObject value) {

        sNC(path, key + ".json", value != null ? value.toStringPretty() : null);

        String cacheKey = cacheKey(path, key);

        if (value == null) {

            json.remove(cacheKey);

        } else {

            json.put(cacheKey, value);

        }

    }

    public static JSONArray getJSONArray(String path, String key, boolean fix) {

        String cacheKey = cacheKey(path, key);

        if (jsonArray.containsKey(cacheKey)) return jsonArray.get(cacheKey);

        String value = gNC(path, key + ".json");

        if (value == null) return fix ? new JSONArray() : null;

        try {

            return new JSONArray(value);

        } catch (JSONException ex) {

            try {

                JSONArray arr = new JSONArray(new JSONArray(new JSONObject(value).keySet()).toList(Long.class));

                setJSONArray(path, key, arr);

                return arr;

            } catch (JSONException exc) {
            }

            sNC(path, key, null);

            return new JSONArray();

        }

    }

    public static void setJSONArray(String path, String key, JSONArray value) {

        sNC(path, key + ".json", value != null ? value.toStringPretty() : null);

        String cacheKey = cacheKey(path, key);

        if (value == null) {

            jsonArray.remove(cacheKey);

        } else {

            jsonArray.put(cacheKey, value);

        }

    }


}
