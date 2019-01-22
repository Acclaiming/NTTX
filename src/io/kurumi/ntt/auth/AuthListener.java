package io.kurumi.ntt.auth;

import io.kurumi.ntt.twitter.*;

public interface AuthListener {
    
    public void onAuth(TwiAccount account);
    
}
