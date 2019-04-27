package io.kurumi.ntt.funcs.admin;

import io.kurumi.ntt.funcs.abs.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.*;
import java.util.*;
import cn.hutool.core.util.*;
import io.kurumi.ntt.utils.*;
import io.kurumi.ntt.twitter.*;
import twitter4j.*;

public class TwitterActions extends Function {

    public static TwitterActions INSTANCE = new TwitterActions();
    
    @Override
    public void functions(LinkedList<String> names) {

        names.add("taction");

    }

    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params) {

        if (!user.developer()) {

            msg.send("Permission denied").exec();

            return;

        }

        if (params.length < 2) {

            msg.send("/tacion <action> <params>").exec();

            return;

        }

        switch (params[0]) {

                case "like" : doLike(user,msg,params[1]);break;

        }

    }

    void doLike(UserData user,Msg msg,String status) {

        long statusId = NTT.parseStatusId(status);

        if (statusId == -1) {

            msg.send("invaild status id or url...").exec();

            return;

        }
        
        long count = TAuth.data.collection.countDocuments();
        
        Msg progress = msg.send("发送中 : 0 / 0 / 0 / " + count).send();

        long success = 0;
        long failed = 0;
        long already = 0;
        
        for (TAuth account : TAuth.data.collection.find()) {
            
            if (user.id.equals(account.user)) continue;
            
            try {
                
                account.createApi().createFavorite(statusId);
                
                success ++;
                
            } catch (TwitterException e) {
                
                if (e.getErrorCode() == 139) {
                    
                    already ++;
                    
                } else {
                    
                    failed ++;
                    
                }
                
            }
            
            progress.edit("发送中 : " + success + " / " + already + " / " + (success + already + failed) + " / " + count).exec();

        }

        
        
    }

}
