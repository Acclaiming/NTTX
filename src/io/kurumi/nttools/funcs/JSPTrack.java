package io.kurumi.nttools.funcs;

import io.kurumi.nttools.fragments.FragmentBase;
import io.kurumi.nttools.utils.UserData;
import io.kurumi.nttools.model.Msg;
import io.kurumi.nttools.twitter.TwiAccount;
import io.kurumi.nttools.twitter.TwitterUI;

public class JSPTrack extends FragmentBase {

    @Override
    public boolean processPrivateMessage(UserData user, Msg msg,boolean point) {
        
        if (point) return false;
        
        if ("jsp".equals(msg.commandName())) {
            
          //  TwitterUI.INSTANCE.choseAccount();
            
            return true;
            
        }
        
        return false;
        
    }
    
}
