package io.kurumi.ntt.db;

import cn.hutool.core.io.*;
import cn.hutool.json.*;
import io.kurumi.ntt.*;
import java.io.*;
import java.util.*;

public class BotDB {

	private static HashMap<String,JSONObject> cache = new HashMap<>();

	private static String cacheKey(String path,String key) {

		return path + "_" + key;

	}

	private static File cacheFile(String path,String key) {

		return new File(new File(path),key);

	}

	public static boolean exists(String path,String key) {

		return cache.containsKey(cacheKey(path,key)) || cacheFile(path,key).exists();

	}
	
	public static JSONObject gNC(String path,String key) {

		try {

			String value = FileUtil.readUtf8String(cacheFile(path,key));

			if (value != null) return new JSONObject(value);

		} catch (IORuntimeException e) {
		}
		
		return new JSONObject();

	}

	public static JSONObject get(String path,String key) {

		String cacheKey = cacheKey(path,key);

		if (cache.containsKey(cacheKey)) return cache.get(cacheKey);

		JSONObject value = gNC(path,key);

		cache.put(cacheKey,value);

		return value;

	}

	public static void sNC(String path,String key,JSONObject value) {

		if (value == null) {

			FileUtil.del(cacheFile(path,key));

		} else {

			FileUtil.writeUtf8String(value.toStringPretty(),cacheFile(path,key));

		}

	}

    public static void set(String path,String key,JSONObject value) {

		sNC(path,key,value);

		String cacheKey = cacheKey(path,key);

		if (value == null) {

			cache.remove(cacheKey);

		} else {

			cache.put(cacheKey,value);

		}

	}

}
