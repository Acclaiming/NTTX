package io.kurumi.ntt.db;

import io.kurumi.ntt.BotConf;

public class Permission {
    
    public static final String KEY = "permission";
    
    public int getPermission(UserData user) {
        
        String permission = BotDB.jedis.hget(KEY,user.id.toString());
       
        if (permission == null) return 0;
        
        return Integer.parseInt(permission);
        
    }
    
    public void setPermission(UserData user,Integer permission) {
        
        BotDB.jedis.hset(KEY,user.id.toString(),permission.toString());
        
    }
    
    public void removePermission(UserData user) {
        
        BotDB.jedis.hdel(KEY,user.id.toString());
        
    }
    
    public boolean isAdmin(UserData user) {
        
        return getPermission(user) == 1 || BotConf.FOUNDER.equals(user.userName);
        
    }
    
    public boolean isBureaucrats(UserData user) {
        
        return getPermission(user) == 2 || BotConf.FOUNDER.equals(user.userName);
        
    }
    

}
