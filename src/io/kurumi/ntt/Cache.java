package io.kurumi.ntt;

import java.util.TimerTask;
import io.kurumi.ntt.disc.TAuth;
import io.kurumi.ntt.db.UserData;

public class Cache extends TimerTask {

    public static Cache INSTANCE = new Cache();
    
    @Override
    public void run() {
        
        UserData.fastCache.clear();
        TAuth.fastCache.clear();
        
    }

    
}
