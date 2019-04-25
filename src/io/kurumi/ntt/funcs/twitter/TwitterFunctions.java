package io.kurumi.ntt.funcs.twitter;

import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.funcs.twitter.login.*;

public class TwitterFunctions {
    
    public static void init(BotFragment fragment) {
        
        fragment.addFragment(TwitterLogin.INSTANCE);
        fragment.addFragment(TwitterLogout.INSTANCE);
        
    }
    
}
