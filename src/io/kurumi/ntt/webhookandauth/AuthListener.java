package io.kurumi.ntt.webhookandauth;

import io.kurumi.ntt.twitter.*;

public interface AuthListener {
    
    public void onAuth(TwiAccount account);
    
}
