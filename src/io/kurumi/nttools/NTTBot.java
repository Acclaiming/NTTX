package io.kurumi.nttools;

import io.kurumi.nttools.fragments.MainFragment;
import java.io.File;
import io.kurumi.nttools.utils.UserData;
import io.kurumi.nttools.model.Msg;
import io.kurumi.nttools.twitter.TwitterUI;

public class NTTBot extends MainFragment {
    
    public NTTBot() {
        
        super(new File("./data"));
        
    }

    @Override
    public void processPrivateMessage(UserData user, Msg msg) {
        
        if (!msg.isCommand()) help(user,msg);
       
        switch(msg.commandName()) {
            
            case "start" : case "help" : help(user,msg); return;
            
        }
        
        TwitterUI.INSTANCE.process(user,msg);
        
    }
    
    public void help(UserData user,Msg msg) {
        
        String[] helpMsg = new String[] {
            
            "这里是奈间家的BOT (◦˙▽˙◦)","",
            
            TwitterUI.INSTANCE.help(user)
            
        };
        
        msg.send(helpMsg).exec();
        
    }
    
}
