package io.kurumi.ntt.twitter.track;

import cn.hutool.json.JSONObject;
import io.kurumi.ntt.db.BotDB;
import io.kurumi.ntt.db.UserData;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map;
import java.util.Set;

public class UserTackTask extends TimerTask {

    static UserTackTask INSTANCE = new UserTackTask();
    static Timer timer = new Timer("NTT Twitter User Track Task");
    
    public static void start() {
        
        timer.schedule(INSTANCE,new Date(),30 * 60 * 1000);
        
    }
    
    @Override
    public void run() {
        
        for (String id : subs.keySet()) {
            
            UserData.INSTANCE.get(Long.parseLong(id));
            
            subs.getJSONArray(id).toList(Long.class);
            
        }
        
    }
    
    public static JSONObject subs = BotDB.getJSON("data","subscriptions",true);

    public static boolean exists(UserData user) {

        return subs.containsKey(user.idStr);

    }

    public static List<Long> get(UserData user) {

        if (subs.containsKey(user.idStr)) {

            return subs.getJSONArray(user.idStr).toList(Long.class);

        }

        return null;

    }

    public static void add(UserData user,Long id) {

        LinkedHashSet<Long> list = subs.containsKey(user.idStr) ? new LinkedHashSet<Long>(subs.getJSONArray(user.idStr).toList(Long.class)) : new LinkedHashSet<>();

        list.add(id);

        subs.put(user.idStr,id);

    }

    public static void rem(UserData user,Long id) {

        LinkedHashSet<Long> list = subs.containsKey(user.idStr) ? new LinkedHashSet<Long>(subs.getJSONArray(user.idStr).toList(Long.class)) : new LinkedHashSet<>();

        list.remove(id);

        subs.put(user.idStr,id);

    }

    public static void clear(UserData user) {

        subs.remove(user.idStr);

    }

    public static void save() {

        BotDB.setJSON("data","subscriptions",subs);

    }



}
