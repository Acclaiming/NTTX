package io.kurumi.ntbot.auth;

import io.kurumi.ntbot.twitter.*;

public interface AuthListener {
    
    public void onAuth(TwiAccount account);
    
}
