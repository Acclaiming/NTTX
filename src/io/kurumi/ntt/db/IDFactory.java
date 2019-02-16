package io.kurumi.ntt.db;

public class IDFactory {
    
    public static final String KEY = "NTT_ID";
    
    public static Long currentId(String key) {

        return Long.parseLong(BotDB.jedis.hget(KEY,key));

    }
    public static Long nextId(String key) {
        
        return BotDB.jedis.hincrBy(KEY,key,1);
        
    }
    
}
