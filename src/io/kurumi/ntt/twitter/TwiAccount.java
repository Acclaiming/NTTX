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
import io.kurumi.ntt.db.IDFactory;
import io.kurumi.ntt.db.UserData;
import java.util.LinkedList;
import io.kurumi.ntt.utils.BotLog;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;

public class TwiAccount extends JSONObject {

    public static final String KEY = "NTT_AUTH";
    public static final String CURR = "NTT_AUTH_CURR";
    
    public Long id;

    public Integer belong;

    public String apiToken;
    public String apiSec;
    public String accToken;
    public String accSec;

    public Long accountId;
    public String screenName;

    public String name;
    public String email;

    public boolean invaild = true;

    public TwiAccount() {

        this.id = -1L;

    }

    public TwiAccount(Long id, String json) {

        super(json);

        this.id = id;

        belong = getInt("belog");

        apiToken = getStr("api_token");
        apiSec = getStr("api_sec");

        accToken = getStr("acc_token");
        accSec = getStr("acc_sec");

        accountId = getLong("accountId", -1L);
        screenName = getStr("screenName");
        name = getStr("name");
        email = getStr("email");

        invaild = false;

    }

    public UserData belong() {

        if (belong == null) return null;

        return UserData.get(belong);

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

        return "[" + name + "](" + url() + ")";

    }

    public boolean refresh() {

        try {

            Twitter api = createApi();
            User thisAcc = api.verifyCredentials();
            accountId = thisAcc.getId();
            screenName = thisAcc.getScreenName();
            name = thisAcc.getName();
            email = thisAcc.getEmail();

            invaild = false;

            return true;


        } catch (TwitterException e) {

            invaild = true;

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

    public void save() {

        put("belong", belong);
        put("api_token", apiToken);
        put("api_sec", apiSec);
        put("acc_token", accToken);
        put("acc_sec", accSec);
        put("account_id", accountId);
        put("screen_name", screenName);
        put("name", name);
        put("email", email);

        if (id == -1L) {

            id = IDFactory.nextId(KEY);

        }


        if (!invaild) {

            BotDB.jedis.hset(KEY, id.toString(), toString());
            
            cache.put(id,this);
            
            if (!curr.containsKey(belong)) {
                
                switchAccount(belong,this);
                
            }

        } else {

            BotLog.warnWithStack("尝试保存无效的Twitter认证 已忽略");

        }

    }

    @Override
    public boolean equals(Object obj) {

        return super.equals(obj) || (obj instanceof TwiAccount && accountId.equals(((TwiAccount)obj).accountId));

    }

    @Override
    public String toString() {

        return screenName + "「" + name + "」";

    }
    
    public void logout() {
        
        if (curr.containsValue(this)) {
            
            curr.remove(belong);
            
            LinkedList<TwiAccount> accounts = getAccounts(belong);

            accounts.remove(this);
            
            if (!accounts.isEmpty()) {
                
                switchAccount(belong,accounts.getFirst());
                
            } else {
                
                BotDB.jedis.hdel(CURR,belong.toString());
                
            }
            
        }
        
        cache.remove(id);
        
        BotDB.jedis.hdel(KEY,id.toString());
        
    }

    private static LinkedHashMap<Long,TwiAccount> cache = new LinkedHashMap<>(); static {
        
        Map<String, String> all = BotDB.jedis.hgetAll(KEY);

        for(Map.Entry<String,String> acc : all.entrySet()) {
            
            long id = Long.parseLong(acc.getKey());
            
            cache.put(id,new TwiAccount(id,acc.getValue()));
            
        }
        
    }
   
    private static HashMap<Integer,TwiAccount> curr = new HashMap<>(); static {
        
        Map<String, String> all = BotDB.jedis.hgetAll(KEY);

        for(Map.Entry<String,String> acc : all.entrySet()) {

            int user = Integer.parseInt(acc.getKey());
            long account = Long.parseLong(acc.getValue());
            
            curr.put(user,cache.get(account));

        }
        
    }
    
    public static TwiAccount getByScreenName(String screenName) {

        for (TwiAccount account : cache.values()) {

            if (screenName.equals(account.screenName)) return account;

        }

        return null;

    }
    
    public static TwiAccount getByAccountId(Long id) {

        for (TwiAccount account : cache.values()) {

            if (id.equals(account.accountId)) return account;

        }

        return null;

    }
    
    public static TwiAccount getById(Long id) {
        
        if (!cache.containsKey(id)) return null;
        
        return cache.get(id);
        
    }
    
    public static LinkedList<TwiAccount> findByName(String name) {

        LinkedList<TwiAccount> accs = new LinkedList<>();

        for (TwiAccount account : cache.values()) {

            if (account.name.contains(name)) accs.add(account);

        }

        return accs;

    }

    public static LinkedList<TwiAccount> getAccounts(Integer id) {
        
        LinkedList<TwiAccount> accs = new LinkedList<>();
        
        for (TwiAccount account : cache.values()) {
            
            if (id.equals(account.belong)) accs.add(account);
            
        }
        
        return accs;
        
    }
    
    public static TwiAccount getCurrent(Integer userId) {
        
        return curr.containsKey(userId) ? curr.get(userId) : null;
        
    }
    
    public static void switchAccount(Integer userId,TwiAccount account) {
        
        curr.put(userId,account);
        
        BotDB.jedis.hset(CURR,userId.toString(),account.id.toString());
        
    }

}
