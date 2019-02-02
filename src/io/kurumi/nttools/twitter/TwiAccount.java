package io.kurumi.nttools.twitter;

import cn.hutool.json.JSONObject;
import io.kurumi.nttools.utils.Markdown;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TwiAccount {

    private String apiToken;
    private String apiSecToken;
    private String accToken;
    private String accSecToken;

    public long accountId;
    public String screenName;
    
    public String name;
 //   public String email;
 
    public JSONObject userData;

    public TwiAccount(JSONObject json) {
        
        this(json.getStr("apiToken"),
             json.getStr("apiSecToken"),
             json.getStr("accToken"),
             json.getStr("accSecToken"));

        accountId = json.getLong("accountId",-1L);
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
    
    public String getMarkdowName() {

        return Markdown.toHtml("「" + Markdown.encode(name) + "」 [(@" + screenName + ")](" + getUrl() + ")");

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

    public JSONObject toJSONObject() {

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
        
        return super.equals(obj) || (obj instanceof TwiAccount && ((TwiAccount)obj).accountId == accountId);
        
    }

    @Override
    public String toString() {
        
        return screenName + "「" + name + "」";

    }
    
}
