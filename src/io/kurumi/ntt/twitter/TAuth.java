package io.kurumi.ntt.twitter;

import cn.hutool.json.JSONObject;
import io.kurumi.ntt.db.SData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.utils.Html;
import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TAuth extends JSONObject {

    public static JSONObject auth = SData.getJSON("data","auth",true);
    
	public static boolean exists(UserData user) {
        
		return auth.containsKey(user.id.toString());
		
	}
    
    public static boolean avilable(UserData user) {
        
       return exists(user) && get(user).refresh();
        
    }
	
	public static TAuth get(UserData user) {
		
		if (exists(user)) {
			
			return new TAuth(auth.getJSONObject(user.id.toString()));
			 
		}
		
		return null;
		
	}
    
    public static void saveAll() {
        
        SData.setJSON("data","auth",auth);
        
    }
	
    private String apiToken;
    private String apiSecToken;
    private String accToken;
    private String accSecToken;

    public Long accountId;
    public String screenName;

    public String name;
    public String email;

    public TAuth(JSONObject json) {

        this(json.getStr("apiToken"),
             json.getStr("apiSecToken"),
             json.getStr("accToken"),
             json.getStr("accSecToken"));

        putAll(json);

        accountId = json.getLong("accountId", -1L);
        screenName = json.getStr("screenName");
        name = json.getStr("name");

        email = json.getStr("email");
    }

    public TAuth(String apiToken, String apiSecToken, String accToken, String accSecToken) {
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

    public String getFormatedNameHtml() {

        return Html.a(name,getUrl());

    }

    public boolean refresh() {

        try {

            Twitter api = createApi();
            User thisAcc = api.verifyCredentials();
            accountId = thisAcc.getId();
            screenName = thisAcc.getScreenName();
            name = thisAcc.getName();
            email = thisAcc.getEmail();
            
            save();
            
            return true;

        } catch (TwitterException e) {
            
            
            
        }

        return false;

    }

    public Twitter createApi() {

        return new TwitterFactory(createConfig()).getInstance();

    }
    
    public AsyncTwitter createAsyncApi() {

        return new AsyncTwitterFactory(createConfig()).getInstance();

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

            return this;
            
    }

    @Override
    public boolean equals(Object obj) {

        return super.equals(obj) || (obj instanceof TAuth && accountId.equals(((TAuth)obj).accountId));

    }

    @Override
    public String toString() {

        return screenName + "「" + name + "」";

    }

}
