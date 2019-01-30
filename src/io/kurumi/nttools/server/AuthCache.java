package io.kurumi.nttools.server;

import com.pengrad.telegrambot.model.Message;
import io.kurumi.nttools.fragments.Fragment;
import io.kurumi.nttools.utils.UserData;
import java.util.HashMap;
import twitter4j.auth.RequestToken;

public class AuthCache {
    
    public static HashMap<String,Listener> cache = new HashMap<>();
    
    public interface Listener {
 
        public abstract void onAuth(String oauthVerifier);
        
    }
    
}
