package io.kurumi.ntt.funcs.twitter;

import cn.hutool.core.util.*;
import cn.hutool.json.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.twitter.archive.*;
import io.kurumi.ntt.twitter.track.*;
import io.kurumi.ntt.utils.*;
import java.util.*;
import twitter4j.*;

import cn.hutool.json.JSONArray;

public class UserTrack extends Fragment {

    public static UserTrack INSTANCE = new UserTrack();
    
    @Override
    public boolean onMsg(UserData user,Msg msg) {

        if (!msg.isCommand()) return false;

        switch (msg.command()) {

            case "sub" : subUser(user,msg);break;
            case "unsub" : unSubUser(user,msg);break;
            case "sublist" : subList(user,msg);break;
            case "unsuball" : unSubAll(user,msg);break;

            default : return false;

        }

        return true;

    }
    
    String notFondMsg(TwitterException ex) {
        
        if (ex.getErrorCode() == 50) {
            
            return "用户不存在 (" + ex.getErrorMessage() + ")";
            
        } else if (ex.getErrorCode() == 63) {
            
            return "用户被冻结/停用 (" + ex.getErrorMessage() + ")";
            
        }
        
        return ex.getErrorCode() + " : " + ex.getErrorMessage();
        
        
    }

    void subUser(UserData user,Msg msg) {

        if (NTT.checkNonContactable(user,msg)) return;
        if (NTT.checkUserNonAuth(user,msg)) return;

        if (msg.params().length != 1) {

            msg.send("用法 : /sub <推油链接/用户名/ID>").exec();

            return;
            
        }

        if (NumberUtil.isNumber(msg.params()[0])) {

            try {

                User target = TAuth.get(user.id).createApi().showUser(Long.parseLong(msg.params()[0]));

                UserArchive archive = UserArchive.save(target);

                boolean result = UTTask.add(user,target.getId());
                
                if (!result) {

                    msg.send("你已经订阅了这个推油 :)").exec();

                    return;

                }
                
                UTTask.save();

                msg.send("已订阅 : " + archive.urlHtml() + " :)","ID : " + archive.id).html().exec();

            } catch (TwitterException e) {

                msg.send("ID :" + msg.params()[0] + " 无法取得 :( ","----------------",notFondMsg(e)).exec();

            }

            return;

        }

        String screenName = NTT.parseScreenName(msg.params()[0]);

        try {

            User target = TAuth.get(user.id).createApi().showUser(screenName);

            boolean result = UTTask.add(user,target.getId());
            
            UserArchive archive = UserArchive.save(target);

            UTTask.save();

            if (!result) {

                msg.send("你已经订阅了这个推油 :)").exec();

                return;

            }
            
            UTTask.save();

            msg.send("已订阅 : " + archive.urlHtml() + " :)","ID : " + archive.id).html().exec();

        } catch (TwitterException e) {

            msg.send("@" + msg.params()[0] + " 无法取得 :(","----------------",notFondMsg(e)).exec();

        }

    }

    void unSubUser(UserData user,Msg msg) {

        if (msg.params().length != 1) {

            msg.send("用法 : /unsub <推油链接/用户名/ID>").exec();
            return;
            
        }

        if (NumberUtil.isNumber(msg.params()[0])) {

            if (UTTask.rem(user,Long.parseLong(msg.params()[0]))) {
                
                UTTask.save();
                
                msg.send("取消订阅成功 :)").exec();
                
            } else {
                
                msg.send("乃没有订阅这个推油 :)").exec();
                
            }
            
            return;

        }
        
        UserArchive target = UserArchive.get(NTT.parseScreenName(msg.params()[0]));

        if (target == null || !UTTask.rem(user,target.id)) {
            
            msg.send("乃没有订阅这个推油 :)").exec();
            
            
        } else {
            
            UTTask.save();
            
            msg.send("已取消订阅 " + target.urlHtml() + " :)").html().exec();
            
        }
        
    }
    
    void subList(UserData user,Msg msg) {
        
        if (UTTask.exists(user)) {

            List<Long> list = UTTask.get(user);

            StringBuilder rec = new StringBuilder();

            for (int index = 0;index < list.size();index ++) {

                rec.append("\n").append(UserArchive.get((list.get(index))).urlHtml());

            }

            msg.send("已订阅 : ",rec.toString()).html().exec();

            UTTask.save();


        } else {

            msg.send("没有订阅 :)").exec();

        }
        
        
    }
    
    void unSubAll(UserData user,Msg msg) {
        
        
        if (UTTask.exists(user)) {
            
            JSONArray list = UTTask.remAll(user);

            StringBuilder rec = new StringBuilder();

            for (int index = 0;index < list.size();index ++) {
                
                rec.append("\n").append(UserArchive.get(list.getLong(index)).urlHtml());
                
            }
            
            msg.send("好,对这些推油的订阅已取消 :)",rec.toString()).html().exec();

            UTTask.save();
            
            
        } else {
            
            msg.send("虽然没有订阅，但还是成功了 function clearCache = alert(\"清理成功\") 哈哈哈 :)").exec();
            
        }
        
    }

}
