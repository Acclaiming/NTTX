package io.kurumi.ntt.twitter.track;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import io.kurumi.ntt.db.BotDB;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.twitter.TAuth;
import io.kurumi.ntt.twitter.archive.UserArchive;
import io.kurumi.ntt.utils.BotLog;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class UserTrackTask extends TimerTask {

    static UserTrackTask INSTANCE = new UserTrackTask();
    static Timer timer;

    public static void start() {

        stop();

        timer = new Timer("NTT Twitter User Track Task");
        timer.schedule(INSTANCE,new Date(),1 * 60 * 1000);

    }

    public static void stop() {

        if (timer != null) timer.cancel();

    }

    int indexG = 0;
    HashMap<String,Integer> useH = new HashMap<>();

    @Override
    public void run() {

        if (subs.isEmpty()) return;

        if (TAuth.auth.isEmpty()) return;

        if (indexG == 15) {

            indexG = 0;

            useH.clear();

        }

        indexG ++;

        HashMap<Long,List<Long>> subIndex = new HashMap<>();
        HashMap<UserArchive,String> changes = new HashMap<>();

        Collection<JSONArray> values = (Collection<JSONArray>)(Object)subs.values();

        List<Long> globals = new LinkedList<>();

        for (JSONArray arr : values) {

            globals.addAll(arr.toList(Long.class));

        }

        globals = new LinkedList<Long>(new LinkedHashSet<Long>(globals));

        if (globals.size() > TAuth.auth.size() * 900 * 100) {

            // TOTO : 不可能的 ~ (flag)

            BotLog.errorWithStack("监听中的总用户数大于API限制");

            return;

        }

        boolean finished = false;

        try {

            int index = 0;

            while (!finished) {
                
                if (index > 850) {
                    
                    // 十五分钟上限900次 到850可以退出等API可用;
                    
                    return;
                    
                }

                for (String id : subs.keySet()) {

                    if (useH.containsKey(id)) {

                        if (useH.get(id) > index) {

                            continue;

                        }

                        useH.put(id,useH.get(id));

                    } else {

                        useH.put(id,1);

                    }
                    

                    UserData user = UserData.INSTANCE.get(Long.parseLong(id));

                    subIndex.put(user.id,subs.getJSONArray(user.idStr).toList(Long.class));

                    if (!TAuth.exists(user)) continue;

                    Twitter api = TAuth.get(user).createApi();

                    List<Long> target;

                    if (globals.size() > 100) {

                        target = globals.subList(0,99);

                        globals = globals.subList(99,globals.size());


                    } else {

                        target = globals;

                        finished = true;

                    }

                    ResponseList<User> result = api.lookupUsers(ArrayUtil.unWrap(target.toArray(new Long[target.size()])));

                    for (User tuser : result) UserArchive.saveCache(tuser);

                }
            }

            if (changes.isEmpty()) return;

            for (Map.Entry<UserArchive,String> change : changes.entrySet()) {

                UserArchive archive = change.getKey();

                if (!subIndex.containsKey(archive.id)) continue;

                List<Long> subscribers = subIndex.get(archive.id);

                for (Long id : subscribers) {

                    new Send(id,archive.getHtmlURL(),change.getValue()).html().exec();

                }

            }

        } catch (TwitterException ex) {

            BotLog.error("UserArchive Failed...",ex);

        }

    }

    public static void onUserChange(UserArchive user,String change) {

        for (Map.Entry<String,JSONArray> sub : ((Map<String,JSONArray>)(Object)subs).entrySet()) {

            if (sub.getValue().contains(user.id)) {

                new Send(Long.parseLong(sub.getKey()),user.getHtmlURL() + " :",change).html().exec();

            }

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

    public static boolean add(UserData user,Long id) {

        LinkedHashSet<Long> list = subs.containsKey(user.idStr) ? new LinkedHashSet<Long>(subs.getJSONArray(user.idStr).toList(Long.class)) : new LinkedHashSet<>();

        boolean result = list.add(id);

        subs.put(user.idStr,list);

        return result;

    }

    public static boolean rem(UserData user,Long id) {

        LinkedHashSet<Long> list = subs.containsKey(user.idStr) ? new LinkedHashSet<Long>(subs.getJSONArray(user.idStr).toList(Long.class)) : new LinkedHashSet<>();

        boolean result = list.remove(id);

        subs.put(user.idStr,list);

        return result;

    }

    public static void clear(UserData user) {

        subs.remove(user.idStr);

    }

    public static void save() {

        BotDB.setJSON("data","subscriptions",subs);

    }



}
