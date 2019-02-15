package io.kurumi.ntt.twitter;

import cn.hutool.json.JSONObject;
import io.kurumi.ntt.utils.Markdown;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import io.kurumi.ntt.db.BotDB;

public class TwiAccount extends JSONObject {

    public Long id;
    
    public Long belong;
    
    private String apiToken;
    private String apiSecToken;
    private String accToken;
    private String accSecToken;

    public Long accountId;
    public String screenName;

    public String name;
    public String email;

    public TwiAccount(JSONObject json) {

        super(json);

        apiToken = getStr("api_token");
        
        accountId = getLong("accountId", -1L);
        screenName = getStr("screenName");
        name = getStr("name");
        email = getStr("email");
        
        
    }


    public String url() {

        return "https://twitter.com/" + screenName;

    }

    public String formatedName() {

        return "「" + name + "」" + " (@" + screenName + ")";

    }

    public String formatedNameHtml() {

        return Markdown.toHtml(formatedNameMarkdown());

    }

    public String formatedNameMarkdown() {

        return "[" + Markdown.encode(name) + "](" + url() + ")";

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

        
        
        put("api_token", apiToken);
        put("api_sec", apiSecToken);
        put("acc_token", accToken);
        put("acc_sec", accSecToken);
        put("account_id", accountId);
        put("screen_name", screenName);
        put("name", name);
        put("email", email);
    
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

