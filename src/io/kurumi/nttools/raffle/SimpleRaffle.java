package io.kurumi.nttools.raffle;

import io.kurumi.nttools.fragments.FragmentBase;
import io.kurumi.nttools.model.Msg;
import io.kurumi.nttools.utils.UserData;
import io.kurumi.nttools.twitter.TwiAccount;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import io.kurumi.nttools.twitter.TApi;
import java.util.LinkedList;
import twitter4j.User;
import java.util.Random;

public class SimpleRaffle extends FragmentBase {
    
    public static final SimpleRaffle INSTANCE = new SimpleRaffle();

    @Override
    public boolean processPrivateMessage(UserData user, Msg msg, boolean point) {
        
        if (point) return false;
        
        if (!"sr".equals(msg.commandName())) return false;
        
        if (user.twitterAccounts.isEmpty()) {
            
            msg.send("认证账号 /twitter").exec();
            
            return true;
            
        }
        
        Twitter api = user.twitterAccounts.getFirst().createApi();

        int count = 1;
        
        if (msg.commandParms().length == 1) {
            
            count = Integer.parseInt(msg.commandParms()[0]);
            
            if (count < 1) {
                
                count = 1;
                
            }
            
        }
        
        try {
            
            if (api.verifyCredentials().getFollowersCount() < count) {

                msg.send("你没有那么多fo ←_←").exec();
                
                return true;

            }
            
            LinkedList<User> fos = TApi.getAllFo(api, api.getId());
            
            Random rand = new Random();

            msg.send("中奖的推油 :").exec();
            
            for (int index = 0;index < count;index ++) {
                
                User target = fos.remove(rand.nextInt(fos.size()));
                
                msg.send((index + 1) + " : " + TApi.formatUserNameMarkdown(target)).markdown().disableLinkPreview().exec();

            }
            
            return true;
            

        } catch (TwitterException e) {
            
            throw new RuntimeException(e);
            
        }
        
       

        
    }
    
}
