package io.kurumi.nttools.spam;

import cn.hutool.json.JSONObject;

public class UserSpam {

    public static Long Permanent = 253392455349L;
    
    public transient SpamList belongTo;

    public Long twitterAccountId;
    public String twitterScreenName;
    public String twitterDisplyName;
    
    public String spamCause;

    public Long untilDate;
    
    public UserSpam() {}
    
    public UserSpam(SpamList list) {
        
        belongTo = list;
        
    }
    
    public UserSpam(SpamList list,JSONObject data) {
        
        this(list);
        
        twitterAccountId = data.getLong("twitter_account_id");

        twitterScreenName = data.getStr("twitter_screen_name");
        twitterDisplyName = data.getStr("twitter_disply_name");
        
        spamCause = data.getStr("spam_cause");
        
        untilDate = data.getLong("until_date");
        
    }
    
    public JSONObject toJSONObject() {
        
        return new JSONObject()
        .put("twitter_account_id",twitterAccountId)
        .put("twitter_screen_name",twitterScreenName)
        .put("twitter_disply_name",twitterDisplyName)
        .put("spam_cause",spamCause)
        .put("until_date",untilDate);
        
    }

    @Override
    public boolean equals(Object obj) {

        return super.equals(obj) ||
            
            (obj instanceof UserSpam &&

            twitterAccountId.equals(((UserSpam)obj).twitterAccountId) &&
            
            belongTo.equals(((UserSpam)obj).belongTo));

    }

}
