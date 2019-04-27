package io.kurumi.ntt.funcs.twitter.ext;

import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.funcs.abs.TwitterFunction;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.twitter.TAuth;
import io.kurumi.ntt.utils.NTT;
import java.util.LinkedList;
import twitter4j.Query;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.QueryResult;
import io.kurumi.ntt.twitter.TApi;

public class StatusLottery extends TwitterFunction {

    @Override
    public void functions(LinkedList<String> names) {
       
        names.add("lottery");
        
    }

    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {
       
        if (params.length != 1) {
            
            msg.send("/lottery <推文ID|链接>").exec();
           
            return;
            
        }
        
        long statusId = NTT.parseStatusId(params[0]);
        
        if (statusId == -1) {
            
            msg.send("无效的推文链接...").exec();
            
            return;
            
        }
        
        Twitter api = account.createApi();

        final Status status;
        
        try {
            
           status = api.showStatus(statusId);
            
        } catch (TwitterException e) {
            
            msg.send("找不到这个推文...").exec();
            
            return;
            
        }
        
        try {
            
            QueryResult result = api.search(new Query()
                                            .query("to:" + status
                                                   .getUser()
                                                   .getScreenName())
                                            .sinceId(status.getId())
                                            .resultType(Query.RECENT));
            
        } catch (TwitterException e) {}

    }
    
}
