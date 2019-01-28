package io.kurumi.ntt.bots.template;

import io.kurumi.ntt.bots.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.ui.*;
import io.kurumi.ntt.ui.confs.*;
import io.kurumi.ntt.ui.request.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import twitter4j.*;
import cn.hutool.core.util.*;
import io.kurumi.ntt.*;

public class TwitterReFriendBot extends UserBot {

    public TwitterReFriendBot(UserData owner,String name) {
        super(owner,name);
    }
   
    public static final String TYPE = "TwitterReFinendBot";
    
    @Override
    public String type() {
        return TYPE;
    }

    public TwitterAcconutsConf twitterAccounts = new TwitterAcconutsConf(this,"开启的账号","accounts");
    public BoolConf ignoreDeafultPhoto = new BoolConf(this,"忽略空头像用户","ignore_default_photo");
    public BoolConf ignoreProtect = new BoolConf(this,"忽略锁推用户","need_photo");
    
    
    @Override
    public void confs(ConfRoot confs) {
        
        confs.add(twitterAccounts);
        confs.add(ignoreProtect);
        
    }

    @Override
    public AbsResuest start(DataObject obj) {
        
        List<TwiAccount> accounts = twitterAccounts.get();

        if (accounts.isEmpty()) {
            
            interrupt();
            
            return obj.reply().alert("没有启用的账号 >_<");
            
        }

        return obj.reply().text("已开启...");
    }

    @Override
    public void startAtBackground() {
        // TODO: Implement this method
    }

    
    
    
    private AtomicBoolean stopped = new AtomicBoolean(false);
    
    public class ReFriendThread extends Thread {

        @Override
        public void run() {
          
            LinkedList<Twitter> apis = new LinkedList<>();
            
            for (TwiAccount account : twitterAccounts.get()) {
                
                apis.add(account.createApi());
                
            }
            
        }
        
        public void reFriend(Twitter api) throws TwitterException {
            
            IDs followers = api.getFollowersIDs(-1);
            
            for (long id : followers.getIDs()) {
                
                
                
            }
            
        }
        
    }
    
}
