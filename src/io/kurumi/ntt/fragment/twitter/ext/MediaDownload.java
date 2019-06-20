package io.kurumi.ntt.fragment.twitter.ext;

import io.kurumi.ntt.fragment.abs.Function;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.db.UserData;
import java.util.LinkedList;
import io.kurumi.ntt.utils.NTT;
import io.kurumi.ntt.fragment.abs.TwitterFunction;
import io.kurumi.ntt.fragment.twitter.TAuth;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.Status;
import twitter4j.MediaEntity;
import cn.hutool.core.util.ArrayUtil;

public class MediaDownload extends TwitterFunction {

    @Override
    public void functions(LinkedList<String> names) {
        
        names.add("media");
        
    }


    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params, TAuth account) {

        
        if (params.length == 0) {
            
            msg.send("/media [推文ID|链接]...").exec();
            
            return;
            
        }
        
        Twitter api = account.createApi();

        for (String statusStr : params) {
            
            long statusId = NTT.parseStatusId(statusStr);
            
            try {
                
                Status status = api.showStatus(statusId);

                MediaEntity[] medias = status.getMediaEntities();

                msg.send(ArrayUtil.join(medias,"\n|")).exec();
                
            } catch (TwitterException e) {
                
                msg.send(NTT.parseTwitterException(e)).exec();
                
            }

        }
        
    }

}
