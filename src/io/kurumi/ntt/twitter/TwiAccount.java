package io.kurumi.ntt.twitter;

import cn.hutool.json.JSONObject;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import io.kurumi.ntt.db.*;

public class TwiAccount extends JSONObject {

	public boolean contains(UserData user) {
		
		return user.ext.containsKey("twitter_auth");
		
	}
	
	public TwiAccount get(UserData user) {
		
		if (contains(user)) {
			
			return new TwiAccount(user.ext.getJSONObject("twitter_auth"));
			
		}
		
		return null;
		
	}
	
    private String apiToken;
    private String apiSecToken;
    private String accToken;
    private String accSecToken;

    public Long accountId;
    public String screenName;

    public String name;
    public String email;

    public JSONObject userData;

    public TwiAccount(JSONObject json) {

        this(json.getStr("apiToken"),
             json.getStr("apiSecToken"),
             json.getStr("accToken"),
             json.getStr("accSecToken"));

        putAll(json);

        accountId = json.getLong("accountId", -1L);
        screenName = json.getStr("screenName");
        name = json.getStr("name");
        userData = json.getJSONObject("userData");

        if (userData == null) {

            userData = new JSONObject();

        }

        email = json.getStr("email");
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

    public String getFormatedNameMarkdown() {

        return "[" + name + "](" + getUrl() + ")";

    }

    public boolean refresh() {

        try {

            Twitter api = createApi();
            User thisAcc = api.verifyCredentials();
            accountId = thisAcc.getId();
            screenName = thisAcc.getScreenName();
            name = thisAcc.getName();
            email = thisAcc.getEmail();
            
            return true;

        } catch (TwitterException e) {
            
            
            
        }

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

    public JSONObject save() {

      
            put("apiToken", apiToken);
            put("apiSecToken", apiSecToken);
            put("accToken", accToken);
            put("accSecToken", accSecToken);
            put("accountId", accountId);
            put("screenName", screenName);
            put("name", name);
            put("email", email);
            put("userData", userData);

            return this;
            
    }

    @Override
    public boolean equals(Object obj) {

        return super.equals(obj) || (obj instanceof TwiAccount && accountId.equals(((TwiAccount)obj).accountId));

    }

    @Override
    public String toString() {

        return screenName + "「" + name + "」";

    }

}
