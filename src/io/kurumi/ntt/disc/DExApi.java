package io.kurumi.ntt.disc;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import io.kurumi.ntt.BotConf;

public class DExApi {
    
    public static String getTelegramByUserId(int userId) {

        JSONArray resp = doQuery("SELECT * FROM user_custom_fields WHERE name = 'user_field_1' AND user_id = " + userId);

        if (resp.isEmpty()) return null;

        return resp.getJSONObject(0).getStr("value");

    }
    
    public static int getUserIdByTelegram(String telegramId) {
        
        JSONArray resp = doQuery("SELECT * FROM user_custom_fields WHERE name = 'user_field_1' AND value = " + telegramId);

        if (resp.isEmpty()) return -1;
        
        return Integer.parseInt(resp.getJSONObject(0).getStr("user_id"));
        
    }
    
    public static JSONArray doQuery(String sql) {
        
        JSONObject conf = new JSONObject();
        
        conf.put("sql",sql);
        
        String resp = HttpUtil.get(BotConf.get(BotConf.DISC_WAPPER), conf);

        if ("failed".equals(resp)) {
            
            throw new RuntimeException("API错误 可能是秘钥错误");
            
        } else if("false".equals(resp)) {
            
            return new JSONArray();
            
        } else {
            
            return new JSONArray(resp);
            
        }
        
    }
    
}
