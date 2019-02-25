package io.kurumi.ntt.disc;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import io.kurumi.ntt.BotConf;
import java.util.HashMap;
import io.kurumi.ntt.utils.BotLog;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

public class DExApi {
    
    public static HashMap<Integer,String> tFastIndex = new HashMap<>();
    public static HashMap<String,Integer> uFastIndex = new HashMap<>();
    
    public static String getTelegramByUserId(int userId) {

        if (tFastIndex.containsKey(userId)) return tFastIndex.get(userId);
        
        JSONArray resp = doQuery("SELECT * FROM user_custom_fields WHERE name = 'user_field_1' AND user_id = " + userId);

        if (resp.isEmpty()) return null;

        String telegramId =  resp.getJSONObject(0).getStr("value");
        
        tFastIndex.put(userId,telegramId);
        uFastIndex.put(telegramId,userId);
        
        return telegramId;
        
    }
    
    public static Integer getUserIdByTelegram(String telegramId) {
        
        if (uFastIndex.containsKey(telegramId)) return uFastIndex.get(telegramId);
        
        JSONArray resp = doQuery("SELECT * FROM user_custom_fields WHERE name = 'user_field_1' AND value = " + telegramId);

        if (resp.isEmpty()) return null;
        
        int userId = Integer.parseInt(resp.getJSONObject(0).getStr("user_id"));
        
        tFastIndex.put(userId,telegramId);
        uFastIndex.put(telegramId,userId);

        return userId;
        
    }
    
    public static JSONArray doQuery(String sql) {
        
        BotLog.debug("desc api query : " + sql);
        
        JSONObject conf = new JSONObject();
        
        conf.put("sql",sql);
        
        String resp = HttpUtil.get(BotConf.DISC_WAPPER, conf);

        BotLog.debug("desc api reply : " + resp);
        
        if ("failed".equals(resp)) {
            
            throw new RuntimeException("API错误 可能是秘钥错误");
            
        } else if("false".equals(resp)) {
            
            return new JSONArray();
            
        } else {
            
            return new JSONArray(resp);
            
        }
        
    }
    
}
