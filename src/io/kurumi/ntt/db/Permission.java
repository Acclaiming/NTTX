package io.kurumi.ntt.db;

import java.util.HashMap;

public class Permission {

    public static final String KEY = "permission";

    public static final HashMap<Integer,Integer> cache = new HashMap<>();
    
    public static int getPermission(Integer user) {

        if (cache.containsKey(user)) return cache.get(user);
        
        String permission = BotDB.jedis.hget(KEY, user.toString());

        if (permission == null) return 0;

        return Integer.parseInt(permission);

    }

    public static void setPermission(Integer user, Integer permission) {

        cache.put(user,permission);
        
        BotDB.jedis.hset(KEY, user.toString(), permission.toString());

    }

    public static void removePermission(Integer user) {

        cache.remove(user);
        
        BotDB.jedis.hdel(KEY, user.toString());

    }

    public static boolean isUser(Integer user) {

        return getPermission(user) > 0;

    }

    public static boolean isAdmin(Integer user) {

        return getPermission(user) > 1;

    }

    public static boolean isBureaucrats(Integer user) {

        return getPermission(user) > 2;

    }


}
