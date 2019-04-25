package io.kurumi.ntt.twitter;

import cn.hutool.json.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.utils.*;
import twitter4j.*;
import twitter4j.conf.*;

import cn.hutool.json.JSONObject;
import java.util.*;
import com.mongodb.client.model.*;
import com.mongodb.client.*;
import io.kurumi.ntt.twitter.archive.*;

public class TAuth {

    public static Data<TAuth> data = new Data<TAuth>(TAuth.class); static {
        
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
    
    public static TAuth getById(Long accountid) {
        
        return data.getById(accountid);
        
    }
    
    public static FindIterable<TAuth> getByUser(Long userId) {

        return data.findByField("user",userId);

    }
    
    public static boolean contains(Long userId) {
        
        return data.countByField("user",userId) > 0;
        
    }

    public Long id;
    public Long user;
    public String apiKey;
    public String apiKeySec;
    public String accToken;
    public String accTokenSec;

    public UserArchive archive() {
        
        UserArchive archive = UserArchive.get(id);

        try {
            
            if (archive == null)  archive = UserArchive.save(createApi().verifyCredentials());
            
            
        } catch (TwitterException e) {}

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
