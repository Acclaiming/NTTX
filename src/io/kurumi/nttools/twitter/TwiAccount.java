package io.kurumi.nttools.twitter;

import cn.hutool.json.*;
import twitter4j.*;
import twitter4j.conf.*;

import cn.hutool.json.JSONObject;

public class TwiAccount {

    private String apiToken;
    private String apiSecToken;
    private String accToken;
    private String accSecToken;

    public Long accountId;
    public String screenName;
    
    public String name;
 //   public String email;
 
    public JSONObject userData;

    public TwiAccount(JSONObject json) {

        this(json.getStr("apiToken"),
             json.getStr("apiSecToken"),
             json.getStr("accToken"),
             json.getStr("accSecToken"));

        accountId = json.getLong("accountId");
        screenName = json.getStr("screenName");
        name = json.getStr("name");
        userData = json.getJSONObject("userData");
        
        if (userData == null) {
            
            userData = new JSONObject();
            
        }
        
      //  email = json.getStr("email");
    }

    public TwiAccount(String apiToken, String apiSecToken, String accToken, String accSecToken) {
        this.apiToken = apiToken;
        this.apiSecToken = apiSecToken;
        this.accToken = accToken;
        this.accSecToken = accSecToken;
    }

    public String getUrl() {
        
        return "https://twitter.com/" + screenName;
        
    }
    
    public String getFormatedName() {
        
        return "「" + name + "」" + " (@" + screenName + ")";
        
    }

    public boolean refresh() {

        try {
            
            Twitter api = createApi();
            User thisAcc = api.verifyCredentials();
            accountId = thisAcc.getId();
            screenName = thisAcc.getScreenName();
            name = thisAcc.getName();
         //   email = thisAcc.getEmail();
            return true;

        } catch (TwitterException e) {}
        
        return false;

    }

    public Twitter createApi() {

        return new TwitterFactory(createConfig()).getInstance();

    }

    public Configuration createConfig() {

        return new ConfigurationBuilder()
            .setOAuthConsumerKey(apiToken)
            .setOAuthConsumerSecret(apiSecToken)
            .setOAuthAccessToken(accToken)
            .setOAuthAccessTokenSecret(accSecToken)
       //    .setUserStreamBaseURL( "https://userstream.twitter.com/2/" )
            .build();

    }

    public JSONObject toJsonObject() {

        return new JSONObject()
            .put("apiToken", apiToken)
            .put("apiSecToken", apiSecToken)
            .put("accToken", accToken)
            .put("accSecToken", accSecToken)
            .put("accountId", accountId)
            .put("screenName", screenName)
            .put("name",name)
         //   .put("email",email)
            .put("userData",userData);

    }

    @Override
    public boolean equals(Object obj) {
        
        if (super.equals(obj)) return true;
        
        if (!(obj instanceof TwiAccount)) return false;
        
        if (((TwiAccount)obj).accountId != accountId) return false;
        
        return true;
        
    }

}
