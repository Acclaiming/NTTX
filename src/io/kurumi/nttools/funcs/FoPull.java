package io.kurumi.nttools.funcs;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.URLUtil;
import com.pengrad.telegrambot.request.SendDocument;
import io.kurumi.nttools.fragments.FragmentBase;
import io.kurumi.nttools.model.Msg;
import io.kurumi.nttools.twitter.TApi;
import io.kurumi.nttools.utils.UserData;
import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

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
        
        msg.send("正在处理 (｡>∀<｡)").exec();
        
        Twitter api = user.twitterAccounts.getFirst().createApi();
       
        
        try {
            
            String target = api.verifyCredentials().getScreenName();
            
            if (msg.commandParms().length == 1) {
                
                target = msg.commandParms()[0];
                
            }
            
            LinkedList<User> fos = TApi.getAllFo(api, target);
            
            StringBuilder resp = new StringBuilder();
            
            for(User fo : fos) {
                
                resp.append(fo.getId()).append(" ");
                resp.append(fo.getScreenName()).append(" ");
                resp.append(fo.getName()).append("\n");
                
                
            }
            
            File cache = new File(msg.fragment.main.dataDir,"cache/fopull/" + target + ".txt");
           
            FileUtil.writeUtf8String(resp.toString(),cache);
            
            msg.sendUpdatingFile();
            
            msg.fragment.bot.execute(new SendDocument(msg.chatId(),cache));

            return true;
            
       } catch (TwitterException e) {
           
           throw new RuntimeException(e);
           
       }

    }
    
}
