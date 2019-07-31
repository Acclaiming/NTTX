package io.kurumi.ntt.fragment.twitter;

import com.mongodb.client.FindIterable;
import io.kurumi.ntt.db.Data;
import io.kurumi.ntt.fragment.twitter.archive.UserArchive;
import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TAuth {

    public static Data<TAuth> data = new Data<TAuth>(TAuth.class);

    public boolean multiUser() {

        return data.countByField("user", user) > 1;

    }

    static {
        
        /*

        JSONObject old = LocalData.getJSON("data","auth",false);

        if (old != null) {

            for (String userIdStr : old.keySet()) {

                TAuth auth = new TAuth();

                JSONObject oldAuth = old.getJSONObject(userIdStr);

                auth.id = oldAuth.getLong("accountId");
                
                auth.user = Long.parseLong(userIdStr);
                
                auth.apiKey = oldAuth.getStr("apiToken");
                auth.apiKeySec = oldAuth.getStr("apiSecToken");
                auth.accToken = oldAuth.getStr("accToken");
                auth.accTokenSec = oldAuth.getStr("accSecToken");
                
                data.setById(auth.id,auth);

            }
            
            LocalData.set("data","auth",null);


        }
        
        */

    }

    public Long id;
    public Long user;
    public String apiKey;
    public String apiKeySec;
    public String accToken;
    public String accTokenSec;

    public static TAuth getById(Long accountid) {

        return data.getById(accountid);

    }

    public static FindIterable<TAuth> getByUser(Long userId) {

        return data.findByField("user", userId);

    }

    public static boolean contains(Long userId) {

        return data.countByField("user", userId) > 0;

    }

    public UserArchive archive() {

        UserArchive archive = UserArchive.get(id);

        try {

            if (archive == null) archive = UserArchive.save(createApi().verifyCredentials());


        } catch (TwitterException e) {
        }

        return archive;

    }

    public Twitter createApi() {

        return new TwitterFactory(createConfig()).getInstance();

    }

    public AsyncTwitter createAsyncApi() {

        return new AsyncTwitterFactory(createConfig()).getInstance();

    }

    public Configuration createConfig() {

        return new ConfigurationBuilder()
                .setOAuthConsumerKey(apiKey)
                .setOAuthConsumerSecret(apiKeySec)
                .setOAuthAccessToken(accToken)
                .setOAuthAccessTokenSecret(accTokenSec)
                //    .setUserStreamBaseURL( "https://userstream.twitter.com/2/" )
                .build();

    }

}
