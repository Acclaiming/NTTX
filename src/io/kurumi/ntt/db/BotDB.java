package io.kurumi.ntt.db;

import io.kurumi.ntt.Env;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import cn.hutool.json.JSONArray;
import cn.hutool.core.io.*;
import java.util.*;

public class BotDB {

	private static HashMap<String,String> cache = new HashMap<>();
	
	private static String cacheKey(String path,String key) {
		
		return path + "_" + key;
		
	}
	
	private static File cacheFile(String path,String key) {
		
		return new File(new File(Env.DATA_DIR,path),key);
		
	}
	
	public static boolean exists(String path,String key) {
		
		return cache.containsKey(cacheKey(path,key)) || cacheFile(path,key).exists();
		
	}
	
	public static LinkedList<String> getAll(String path) {
		
		// TODO
		
		return null;
		
	}
	
	public static String gNC(String path,String key) {
		
		try {
		
			String value = FileUtil.readUtf8String(cacheFile(path, key));
			
			return value;

		} catch (IORuntimeException e) {

			return null;

		}
		
		}
	
	public static String get(String path,String key) {
		
		String cacheKey = cacheKey(path,key);
		
		if (cache.containsKey(cacheKey)) return cache.get(cacheKey);
		
			String value = gNC(path, key);
			
			cache.put(cacheKey,value);
			
			return value;

		
	}
	
	public static void sNC(String path,String key,String value) {
		
		if (value == null) {
			
			FileUtil.del(cacheFile(path,key));

		} else {

			FileUtil.writeUtf8String(value,cacheFile(path,key));

		}
		
	}
	
    public static void set(String path,String key,String value) {

		sNC(path,key,value);
		
		String cacheKey = cacheKey(path,key);
		
		if (value == null) {
			
			cache.remove(cacheKey);
			
		} else {
			
			cache.put(cacheKey,value);
			
		}
		
	}
    
}
