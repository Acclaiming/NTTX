package io.kurumi.nttools.server;

import java.util.HashMap;

public class AuthCache {
    
    public static HashMap<String,Listener> cache = new HashMap<>();
    
    public interface Listener {
 
        public abstract void onAuth(String oauthVerifier);
        
    }
    
}
