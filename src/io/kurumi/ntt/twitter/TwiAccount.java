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

public class TwiAccount extends JSONObject {

    public static final String KEY = "NTT_AUTH";

    public static final String BELOG_KEY = "NTT_AUTH_BELONG";

    public Long id;

    public Integer belong;

    private String apiToken;
    private String apiSec;
    private String accToken;
    private String accSec;

    public Long accountId;
    public String screenName;

    public String name;
    public String email;

    public Boolean invaild;

    public TwiAccount() {

        this.id = -1L;

    }

    public TwiAccount(Long id, JSONObject json) {

        super(json);

        this.id = id;

        invaild = getBool("invaild", false);

        if (invaild) {

            return;

        }

        belong = getInt("belog");

        apiToken = getStr("api_token");
        apiSec = getStr("api_sec");

        accToken = getStr("acc_token");
        accSec = getStr("acc_sec");

        accountId = getLong("accountId", -1L);
        screenName = getStr("screenName");
        name = getStr("name");
        email = getStr("email");

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

            save();

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

        put("invaild", invaild);

        if (!invaild) {

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

        } 

        if (id != -1) {

            BotDB.jedis.lpush(KEY, toString());

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

    public static LinkedList<TwiAccount> cache = new LinkedList<>();

    
    
}
