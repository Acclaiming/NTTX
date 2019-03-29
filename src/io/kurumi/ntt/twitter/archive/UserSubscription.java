package io.kurumi.ntt.twitter.archive;

import io.kurumi.ntt.db.BotDB;
import cn.hutool.json.JSONObject;
import io.kurumi.ntt.db.UserData;
import java.util.LinkedList;

public class UserSubscription {
    
    public static JSONObject subs = BotDB.getJSON("data","subscriptions",true);
    
    public static boolean exists(UserData user) {
        
        return subs.containsKey(user.idStr);
        
    }
    
 //   public static LinkedList<Long> get() {}
    
}
