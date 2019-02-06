package io.kurumi.nttools.timer;

import io.kurumi.nttools.utils.UserData;
import io.kurumi.nttools.twitter.TwiAccount;
import io.kurumi.nttools.fragments.MainFragment;
import java.util.LinkedList;

public class FollowerTrackTask implements TimerTask {
    
    public static boolean isNoticeOpen(TwiAccount user) {
        
        Boolean open = user.getByPath("follower_track.notice",Boolean.class);
        
        if (open == null) open = false;
        
        return open;
        
    }
    
    public static void setNoticeOpen(TwiAccount user,boolean open) {

        user.putByPath("follower_track.notice",Boolean.class);
        user.save();
        
    }
    
    public static void setStatusOpen(TwiAccount user,boolean open) {

        user.putByPath("follower_track.send_status",Boolean.class);

    }
    
    
    public static boolean isStatusOpen(TwiAccount user) {

        Boolean open = user.getByPath("follower_track.send_status",Boolean.class);

        if (open == null) open = false;

        return open;

    }
    
  //  public static LinkedList<Long> getOld(FragmTwiAccount account) {
        
        
        
  //  }

    @Override
    public void run(MainFragment fragment) {
        
        for (UserData user : fragment.getUsers()) {
            
            for (TwiAccount account : user.twitterAccounts) {
                
                if (isNoticeOpen(account) || isStatusOpen(account)) {
                    
                    
                    
                    
                }
                
            }
            
        }
        
    }
    
}
