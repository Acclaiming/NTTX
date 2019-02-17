package io.kurumi.ntt.db;

import java.util.Map;
import io.kurumi.ntt.utils.BotLog;

public class WrongUse {
    
    public static final String KEY = "NTT_WU";
    
    public static String incrWithMsg(UserData user) {

        BotLog.debug(user.name() + " 又用错了一次！");

        return "你已经用错 " + incr(user) + " 次了！ （￣～￣)";

    }
    
    public static Long incr(UserData user) {
        
        BotLog.debug(user.name() + " 又用错了一次！");
        
        return BotDB.jedis.hincrBy(KEY,user.id.toString(),1);
        
    }
    
    public static Long get(UserData user) {

        return Long.parseLong(BotDB.jedis.hget(KEY,user.id.toString()));

    }
    
}
