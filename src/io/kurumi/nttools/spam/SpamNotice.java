package io.kurumi.nttools.spam;

import io.kurumi.nttools.fragments.Fragment;
import io.kurumi.nttools.model.request.Send;

public class SpamNotice {
    
    public static final String CHANNEL_ID = "@NTTSpamPublic";
    
    private static Fragment fragment;
    
    public static void init(Fragment fragment) {
        
        SpamNotice.fragment = fragment;
        
    }
    
    public static void newSpam(UserSpam spam) {
        
        String[] newSpamMsg = new String[] {
            
            "Twitter 用户 " + spam.twitterAccountId
            
        };
        
        new Send(fragment,CHANNEL_ID,"");
        
    }
    
}
