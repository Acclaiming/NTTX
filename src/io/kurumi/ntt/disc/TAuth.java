package io.kurumi.ntt.disc;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import io.kurumi.ntt.BotConf;
import io.kurumi.ntt.utils.Markdown;
import java.util.HashMap;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import java.util.LinkedList;

public class TAuth {

    public static HashMap<Integer,TAuth> fastCache = new HashMap<>();
    
    public static LinkedList<TAuth> getAll() {
        
        JSONArray resp = DExApi.doQuery("SELECT * FROM user_associated_accounts WHERE provider_name = 'twitter'");

        LinkedList<TAuth> all =  new LinkedList<>();

        if (!resp.isEmpty()) {
            
            for (int index = 0;index < resp.size();index ++) {
                
                all.add(parseAuth(resp.getJSONObject(index)));
                
            }
            
        }
        
        return all;
        
        
    }
    
    public static TAuth get(DUser user) {
        
        if (fastCache.containsKey(user.id)) return fastCache.get(user.id);
        
        JSONArray resp = DExApi.doQuery("SELECT * FROM user_associated_accounts WHERE provider_name = 'twitter' AND user_id = '" + user.id + "'");

        if (resp.isEmpty()) return null;

        TAuth auth = parseAuth(resp.getJSONObject(0));
        
        fastCache.put(auth.origin.id,auth);

        return auth;
        
    }
    
    private static TAuth parseAuth(JSONObject accObj) {
        
        TAuth account = new TAuth();
        
        account.origin = DUser.get(accObj.getStr("user_id"));

        account.accountId = accObj.getLong("provider_uid");

        account.name = accObj.getByPath("info.name",String.class);

        account.screenName = accObj.getByPath("info.nickname",String.class);

        account.email = accObj.getByPath("info.email",String.class);

        account.apiToken = DSiteSitting.getTwiitterConsumerKey();

        account.apiSec =  DSiteSitting.getTwitterConsumerSecret();

        account.accToken = accObj.getByPath("credentials.token",String.class);

        account.accSec = accObj.getByPath("credentials.secret",String.class);
        
        return account;
        
    }
    
    public DUser origin;
    
    public String apiToken;
    public String apiSec;
    public String accToken;
    public String accSec;
    
    public Long accountId;
    public String screenName;
    public String name;
    public String email;
    
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

        return "[" + name + "](" + url() + ")";

    }

    public boolean refresh() {

        try {

            Twitter api = createApi();
            User thisAcc = api.verifyCredentials();
            accountId = thisAcc.getId();
            screenName = thisAcc.getScreenName();
            name = thisAcc.getName();
          //  email = thisAcc.getEmail();

            return true;


        } catch (TwitterException e) {

            return false;
        }

    }

    public Twitter createApi() {

        return new TwitterFactory(createConfig()).getInstance();

    }

    public Configuration createConfig() {

        return new ConfigurationBuilder()
                .setOAuthConsumerKey(apiToken)
                .setOAuthConsumerSecret(apiSec)
                .setOAuthAccessToken(accToken)
                .setOAuthAccessTokenSecret(accSec)
                .build();

    }

    @Override
    public boolean equals(Object obj) {

        return super.equals(obj) || (obj instanceof TAuth && accountId.equals(((TAuth) obj).accountId));

    }

    @Override
    public String toString() {

        return screenName + "「" + name + "」";

    }

}
