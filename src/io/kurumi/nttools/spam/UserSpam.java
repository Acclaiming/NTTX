package io.kurumi.nttools.spam;

import cn.hutool.json.JSONObject;

public class UserSpam extends JSONObject {

    public SpamList belongTo;

    public Long origin;

    public Long twitterAccountId;
    public String twitterScreenName;
    public String twitterDisplyName;

    public String spamCause;

    public Integer public_message_id;

    public UserSpam(SpamList list) {

        belongTo = list;

    }

    public UserSpam(SpamList list, JSONObject data) {

        this(list);

        putAll(data);
        
        origin = getLong("origin");

        twitterAccountId = getLong("twitter_account_id");

        twitterScreenName = getStr("twitter_screen_name");
        twitterDisplyName = getStr("twitter_disply_name");

        spamCause = getStr("spam_cause");

        public_message_id = getInt("public_message_id");

    }

    public JSONObject save() {

        put("origin", origin);
        put("twitter_account_id", twitterAccountId);
        put("twitter_screen_name", twitterScreenName);
        put("twitter_disply_name", twitterDisplyName);
        put("spam_cause", spamCause);
        put("public_message_id", public_message_id);

        return this;

    }

    @Override
    public boolean equals(Object obj) {

        return super.equals(obj) ||

            (obj instanceof UserSpam &&

            twitterAccountId.equals(((UserSpam)obj).twitterAccountId) &&

            belongTo.equals(((UserSpam)obj).belongTo));

    }

}
