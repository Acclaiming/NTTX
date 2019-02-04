package io.kurumi.nttools.spam;

import cn.hutool.json.JSONObject;

public class UserSpam {

    public SpamList belongTo;

    public Long origin;

    public Long twitterAccountId;
    public String twitterScreenName;
    public String twitterDisplyName;

    public String spamCause;

    public Integer vote_message_id;
    
    public UserSpam(SpamList list) {

        belongTo = list;

    }

    public UserSpam(SpamList list, JSONObject data) {

        this(list);

        twitterAccountId = data.getLong("twitter_account_id");

        twitterScreenName = data.getStr("twitter_screen_name");
        twitterDisplyName = data.getStr("twitter_disply_name");

        spamCause = data.getStr("spam_cause");
        
        vote_message_id = data.getInt("vote_message_id");

    }

    public JSONObject toJSONObject() {

        return new JSONObject()
            .put("origin", origin)
            .put("twitter_account_id", twitterAccountId)
            .put("twitter_screen_name", twitterScreenName)
            .put("twitter_disply_name", twitterDisplyName)
            .put("spam_cause", spamCause)
            .put("vote_message_id", vote_message_id);

    }

    @Override
    public boolean equals(Object obj) {

        return super.equals(obj) ||

            (obj instanceof UserSpam &&

            twitterAccountId.equals(((UserSpam)obj).twitterAccountId) &&

            belongTo.equals(((UserSpam)obj).belongTo));

    }

}
