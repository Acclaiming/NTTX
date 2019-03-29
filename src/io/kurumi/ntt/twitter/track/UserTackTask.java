package io.kurumi.ntt.twitter.track;

import java.util.TimerTask;
import java.util.Timer;
import java.util.Date;

public class UserTackTask extends TimerTask {

    static UserTackTask INSTANCE = new UserTackTask();
    static Timer timer = new Timer("NTT Twitter User Track Task");
    
    public static void start() {
        
        timer.schedule(INSTANCE,new Date(),30 * 60 * 1000);
        
    }
    
    @Override
    public void run() {
    }

}
