package io.kurumi.ntt.fragment.admin;

import io.kurumi.ntt.fragment.abs.TwitterFunction;
import java.util.LinkedList;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.abs.Msg;
import twitter4j.Twitter;
import io.kurumi.ntt.fragment.twitter.TApi;
import io.kurumi.ntt.utils.NTT;
import twitter4j.TwitterException;
import twitter4j.Status;
import io.kurumi.ntt.fragment.twitter.archive.StatusArchive;

public class TASReply extends TwitterFunction {

    @Override
    public void functions(LinkedList<String> names) {
        
        names.add("tas");
        
    }

    @Override
    public boolean async() {
       
        return true;
        
    }
    
    

    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params, TAuth account) {
        
        if (!user.developer()) {
            
            return;
            
        }
        
        if (params.length == 0) {
            
            msg.send("/tas <推文ID/链接>").exec();
            
            return;
            
        }
        
        Twitter api = account.createApi();
        
        Status status;
        LinkedList<Status> replies = new LinkedList<>();
        
        int count = 0;
        
        try {
            
            status = api.showStatus(NTT.parseStatusId(params[0]));
           // replies = TApi.getReplies(api, status);

        } catch (TwitterException e) {
            
            msg.send(NTT.parseTwitterException(e)).exec();
            
            return;
            
        }
        
        for (TAuth auth : TAuth.data.collection.find()) {
            
            if (auth.id.equals(account.id)) continue;
            
            try {

                LinkedList<Status> ann = TApi.getReplies(auth.createApi(), status);

                ann.removeAll(replies);
                
                for (Status hide : ann) {
                    
                    count ++;
                    
                    StatusArchive.save(hide).sendTo(msg.chatId(),1,account,hide);
                    
                }
                
                replies.addAll(ann);
                
                
            } catch (TwitterException e) {
                
                
            }
            
        }
        
        msg.send("完成 发现 " + count + "条 隐藏推文").exec();

    }
    
}
