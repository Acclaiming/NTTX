package io.kurumi.nttools.spam;

import io.kurumi.nttools.twitter.TwiAccount;
import twitter4j.Twitter;
import io.kurumi.nttools.twitter.TApi;
import twitter4j.TwitterException;
import java.util.LinkedList;
import twitter4j.User;
import cn.hutool.core.util.ArrayUtil;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SpamTask implements Runnable {

    public static ExecutorService exec; static {
        
        exec = Executors.newSingleThreadExecutor();
        
    }
    
    public SpamList list;
    public TwiAccount account;

    public SpamTask(SpamList list, TwiAccount account) {
        this.list = list;
        this.account = account;
    }
    
    public void start() {
        
        exec.execute(this);
        
    }
    
    @Override
    public void run() {
       
        Twitter api = account.createApi();
        
        try {
            
            long[] blocks = TApi.getAllBlockIDs(api);

            for (UserSpam spamUser : list.spamUsers) {
                
                if (!ArrayUtil.contains(blocks,spamUser.twitterAccountId)) {
                    
                    api.createBlock(spamUser.twitterAccountId);
                    
             //       api.reportSpam(spamUser.twitterAccountId);
                    
                }
                
            }
            
        } catch (Exception e) {
            
            e.printStackTrace();
            
        }

    }
    
    
}
