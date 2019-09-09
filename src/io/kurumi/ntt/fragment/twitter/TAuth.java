package io.kurumi.ntt.fragment.twitter;

import cn.hutool.core.util.RandomUtil;
import com.mongodb.client.FindIterable;
import io.kurumi.ntt.db.Data;
import io.kurumi.ntt.fragment.twitter.archive.UserArchive;
import twitter4j.*;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TAuth {

    public static Data<TAuth> data = new Data<TAuth>(TAuth.class);

    public boolean multiUser() {

        return data.countByField("user", user) > 1;

    }

    private static TAuth current;
    private static AtomicInteger count = new AtomicInteger(0);

    public static TAuth next() {

        synchronized (count) {

            if (current == null || count.incrementAndGet() == 100) {

                count.set(0);

                int max = (int) data.collection.count();

                int target = RandomUtil.randomInt(max);

                current = data.collection.find().skip(target).limit(1).first();

            }

            return current;

        }

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

    public Boolean pm;

    public Boolean mrt;

    public Boolean fo;

    public Boolean fo_marge;
    public List<Long> fo_new;
    public List<Long> fo_lost;

    public List<Long> nf;
    public List<Long> lf;

    public Boolean fo_info;
    public Boolean fr_info;

    public Boolean ign_target;

    public Boolean tl;
    public Long tl_offset;

    public Boolean tl_dn;
    public Boolean tl_ns;
    public Boolean tl_na;
    public Boolean tl_nr;
    public Boolean tl_nt;
    public Boolean tl_nesu;

    public Boolean mention;

    public Long mention_offset;
    public Long rt_offset;

    public Boolean mdb;

    public Boolean ad_s;
    public Boolean ad_r;
    public Boolean ad_t;

    public Boolean ad_a;
    public Integer ad_d;

    public Boolean oup;
    public String oup_msg;

    public Boolean bbb;
    public Boolean bbp;
    public String bbp_msg;

    public Boolean fb;
    public Boolean fbi;
    public Boolean fbp;
    public Boolean fbp_msg;
	
	
	/*
	
	public boolean directMessages = false;
	public long directMessageOffset = -1;
	
	*/

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
