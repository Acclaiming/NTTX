package io.kurumi.ntt.db;

import cn.hutool.json.JSONObject;
import java.util.HashMap;

public class UserData extends JSONObject {

    public static final String KEY = "NTT_USERS";

    public Long id;

    public String name;

    public String userName;

    public boolean isBot;

    private UserData(Long id) {

        this.id = id; 

    }
    
    private UserData(Long id,String json) {

        super(json);
        
        this.id = id;
        
        this.userName = getStr("u");
        
        this.name = getStr("n");
        
        this.isBot = getBool("i",false);

    }

    public void save() {

        put("u", userName);

        put("n", name);
        
        put("i",isBot);

        BotDB.jedis.hset(KEY, id.toString(), toString());

    }

    private static HashMap<Long,UserData> cache = new HashMap<>();

    public static UserData getByUserName(String userName) {
        
        for (UserData user : cache.values()) {}
        
        return null;
        
    }
    
    public static UserData get(Long id) {

        if (cache.containsKey(id)) return cache.get(id);
        
        String data = BotDB.jedis.hget(KEY, id.toString());

        if (data == null) {
            
            UserData user = new UserData(id);
            
            cache.put(id,user);
            
            user.save();
            
            return user;

        }
        
        UserData user = new UserData(id,data);
        
        cache.put(id,user);
        
        return user;
        
    }

}
