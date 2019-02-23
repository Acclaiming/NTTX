package io.kurumi.ntt.disc;

import cn.hutool.json.JSONArray;
import io.kurumi.ntt.utils.BotLog;

public class DSiteSitting {

    private static String getSettingStr(String key) {

        JSONArray resp = DExApi.doQuery("SELECT * FROM site_settings WHERE name = '" + key + "'");

        if (resp.isEmpty()) {
            
            BotLog.error("取Disc设置为空 : " + key);
            
            return null;
            
        }
        
        return resp.getJSONObject(0).getStr("value");

    }

    private static String twitterConsumerKey;

    public static String getTwiitterConsumerKey() {

        if (twitterConsumerKey == null) twitterConsumerKey = getSettingStr("twitter_consumer_key");

        return twitterConsumerKey;

    }

    private static String twitterConsumerSecret;
    
    public static String getTwitterConsumerSecret() {
        
        if (twitterConsumerSecret == null) twitterConsumerSecret = getSettingStr("twitter_consumer_secret");
        
        return twitterConsumerSecret;
        
    }

}
