package io.kurumi.nttools.spam;

import cn.hutool.json.JSONObject;

public class UserSpam {

    public transient SpamList belongTo;

    public Long twitterAccountId;

    public String spamCause;

    public Long untilDate;
    
    public UserSpam() {}
    
    public UserSpam(SpamList list,JSONObject obj) {
        
        belongTo = list;
        
        twitterAccountId = obj.getLong("twitter_account_id");
        
        spamCause = obj.getStr("spam_cause");
        
    }

    @Override
    public boolean equals(Object obj) {

        return super.equals(obj) ||
            
            (obj instanceof UserSpam &&

            twitterAccountId.equals(((UserSpam)obj).twitterAccountId) &&
            
            belongTo.equals(((UserSpam)obj).belongTo));

    }

}
