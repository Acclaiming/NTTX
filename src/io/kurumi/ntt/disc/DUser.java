package io.kurumi.ntt.disc;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import java.util.HashMap;

public class DUser {
    
    public static HashMap<Integer,DUser> idIndex = new HashMap<>();
    public static HashMap<String,DUser> unIndex = new HashMap<>();
    
    public static DUser get(int id) {
        
        if (idIndex.containsKey(id)) return idIndex.get(id);
        
        JSONArray resp = DExApi.doQuery("SELECT * FROM users WHERE id = '" + id + "'");

        if (resp.isEmpty()) { return null; }
        
        return addCache(resp.getJSONObject(0));
        
    }
    
    public static DUser get(String userName) {

        if (unIndex.containsKey(userName)) return unIndex.get(userName);

        JSONArray resp = DExApi.doQuery("SELECT * FROM users WHERE user_name = '" + userName + "'");

        if (resp.isEmpty()) return null;

        return addCache(resp.getJSONObject(0));

    }
    
    
    private static DUser addCache(JSONObject obj) {
        
        DUser user = new DUser();
        
        user.id = obj.getInt("id");
        user.name = obj.getStr("name");
        user.userName = obj.getStr("user_name");

        user.active = obj.getBool("active");
        user.admin = obj.getBool("admin");
        user.moderator = obj.getBool("moderator");
        user.staged = obj.getBool("staged");
        
        user.trustLevel = obj.getInt("trust_level");
        
        idIndex.put(user.id,user);
        unIndex.put(user.name,user);
        
        return user;
        
    }
    
    public Integer id;
    public String name;
    public String userName;
    
    public Boolean active;
    public Boolean admin;
    public Boolean moderator;
    public Boolean staged;
    
    public Integer trustLevel;
    
}
