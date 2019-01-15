package io.kurumi.nt;

import cn.hutool.json.*;
import twitter4j.*;
import twitter4j.conf.*;

import cn.hutool.json.JSONObject;

public class ApiToken {

    public static final ApiToken defaultToken = new ApiToken("Twitter for 奈间", "ygCoKRB56hIl9c57xVDFlr3AB", "tx6BkPwYIW9TV0zQa2HKxWsF5fgyyawhOcoiXRCWmSaohGJuYz");
    public static final ApiToken twidereToken = new ApiToken("Twitter for Android", "0WEJk1x6AlgtjGRhyABXw", "gWXNqEFhO3fMkAqoIKpTdjK0MOJs68xnOky0FRdDTP8");

    public String apiName;
    public String apiToken;
    public String apiSecToken;
    
    public ApiToken(JSONObject json) {
        this(json.getStr("apiName"),json.getStr("apiToken"),json.getStr("apiSecToken"));
    }
    
    public ApiToken(String apiName, String apiToken, String apiSecToken) {
        this.apiName = apiName;
        this.apiToken = apiToken;
        this.apiSecToken = apiSecToken;
    }

    public Configuration createAppOnlyConfig() {

        return new ConfigurationBuilder()
            .setOAuthConsumerKey(apiToken)
            .setOAuthConsumerSecret(apiSecToken)
            .setApplicationOnlyAuthEnabled(true)
            .build();

    }
    
    public Configuration createConfig() {
        
        return new ConfigurationBuilder()
        .setOAuthConsumerKey(apiToken)
        .setOAuthConsumerSecret(apiSecToken)
        .build();
        
    }
    
    public Twitter createAppOnlyApi() {

        return new TwitterFactory(createAppOnlyConfig()).getInstance();

    }
    
    public Twitter createApi() {
        
        return new TwitterFactory(createConfig()).getInstance();
        
    }
  

    public JSONObject toJSONObject() {

        return new JSONObject()
            .put("apiName", apiName)
            .put("apiToken", apiToken)
            .put("apiSecToken", apiSecToken);

    }

}
