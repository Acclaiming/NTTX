package io.kurumi.nttools.funcs;

import io.kurumi.nttools.fragments.FragmentBase;
import io.kurumi.nttools.model.Msg;
import io.kurumi.nttools.utils.UserData;
import twitter4j.Twitter;
import io.kurumi.nttools.twitter.TApi;
import twitter4j.TwitterException;
import java.util.LinkedList;
import twitter4j.User;
import com.pengrad.telegrambot.request.SendDocument;
import cn.hutool.core.io.FileUtil;
import java.io.File;
import java.util.Date;

public class FoPull extends FragmentBase {

    public static final FoPull INSTANCE = new FoPull();
    
    @Override
    public boolean processPrivateMessage(UserData user, Msg msg, boolean point) {
        
        if (point) return false;
        
        if (!"fopull".equals(msg.commandName())) return false;
        
        if (user.twitterAccounts.isEmpty()) {
            
            msg.send("请 /twitter 认证账号").exec();
            
            return true;
            
        }
        
        Twitter api = user.twitterAccounts.getFirst().createApi();
        
        try {
            
            LinkedList<User> fos = TApi.getAllFo(api, api.getId());
            
            StringBuilder resp = new StringBuilder();
            
            for(User fo : fos) {
                
                resp.append(fo.getId()).append(" ");
                resp.append(fo.getScreenName()).append(" ");
                resp.append(fo.getName()).append("\n");
                
                
            }
            
            File cache = new File(msg.fragment.main.dataDir,"cache/fopull/" + api.verifyCredentials().getName() + " - " + new Date().toLocaleString() + ".txt");
           
            FileUtil.writeUtf8String(resp.toString(),cache);
            
            msg.sendUpdatingFile();
            
            msg.fragment.bot.execute(new SendDocument(msg.chatId(),cache));

            return true;
            
       } catch (TwitterException e) {
           
           throw new RuntimeException(e);
           
       }

    }
    
}
