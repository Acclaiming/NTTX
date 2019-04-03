package io.kurumi.ntt.twitter.track;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.request.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.twitter.archive.*;
import io.kurumi.ntt.utils.*;
import java.util.*;
import twitter4j.*;
import cn.hutool.json.JSONObject;
import io.kurumi.ntt.db.BotDB;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.twitter.TApi;
import io.kurumi.ntt.twitter.TAuth;
import io.kurumi.ntt.twitter.archive.UserArchive;
import io.kurumi.ntt.utils.BotLog;
import io.kurumi.ntt.utils.Html;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import twitter4j.Relationship;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class FollowerTrackTask extends TimerTask {

    static FollowerTrackTask INSTANCE = new FollowerTrackTask();
    
    public static JSONObject enable = BotDB.getJSON("data","track",true);
    static HashMap<Long,LinkedList<Long>> cache = new HashMap<>();

    static Timer timer;
    public static void start() {

        stop();

        timer = new Timer("NTT Twitter Follower Track Task");
        timer.schedule(INSTANCE,new Date(),5 * 60 * 1000);

    }

    public static void stop() {

        if (timer != null) timer.cancel();


    }

    public static void save() {

        BotDB.setJSON("data","track",enable);

    }

    @Override
    public void run() {

        for (Map.Entry<String,Object> entry : enable.entrySet()) {

            long userId = Long.parseLong(entry.getKey());

            if (!(boolean)entry.getValue()) continue;

            startUserStackAsync(userId);

        }

    }

    ExecutorService userTrackPool = Executors.newFixedThreadPool(3);

    private void startUserStackAsync(final long userId) {

        userTrackPool.execute(new Runnable() {

                @Override
                public void run() {

                    startUserStack(userId);

                }

            });

    }

    void startUserStack(long userId) {

        try {

            UserData user = UserData.INSTANCE.get(userId);

            if (!TAuth.exists(user)) {

                enable.remove(user.idStr);

                BotDB.setJSONArray("cache","track/" + user.idStr,null);

                save();

                return;

            }

            Twitter api = TAuth.get(user).createApi();

            User me = api.verifyCredentials();

            UserArchive.saveCache(me);

            LinkedList<Long> last = cache.containsKey(api.getId()) ? cache.get(api.getId()) : null;

            LinkedList<Long> latest = TApi.getAllFoIDs(api,api.getId());

            cache.put(api.getId(),latest);

            //    List<Long> pedding = new LinkedList<>();

            for (long id : latest) {

                if (last != null) {

                    if (!last.remove(id)) {

                        newFollower(user,api,id);

                    } else {

                        if (!UserArchive.INSTANCE.exists(id)) {

                            //    pedding.add(id);

                        }

                    }

                }

            }

            if (last != null && last.size() > 0) {

                for (int index = 0;index < last.size();index ++) {

                    long id = last.get(index);

                    lostFollower(user,api,id);

                }

            }

            /*

             if (pedding.size() > 10000) {

             pedding = pedding.subList(0,10000);

             }

             while (pedding.size() > 0) {

             List<Long> target;

             if (pedding.size() > 100) {

             target = pedding.subList(0,100);

             pedding = pedding.subList(99,pedding.size());

             } else {

             target = pedding;

             pedding.clear();

             }

             ResponseList<User> result = api.lookupUsers(ArrayUtil.unWrap(target.toArray(new Long[target.size()])));

             for (User tuser : result) UserArchive.saveCache(tuser);

             }

             */

        } catch (TwitterException e) {

            BotLog.error("UserArchive ERROR",e);

        }

    }

    String link = Html.a("姬生平","https://esu.wiki/姬生平");

    String parseStatus(Twitter api,User user) {

        StringBuilder status = new StringBuilder();

        if (user.isProtected()) status.append("这是一个是锁推用户 :)\n");
        if (user.isFollowRequestSent()) status.append("乃发送了关注请求 :)");
        if (user.getStatusesCount() == 0) status.append("这个用户没有发过推 :)");
        if (user.getFavouritesCount() == 0) status.append("这个用户没有喜欢过推文 :)");
        if (user.getFollowersCount() < 20) status.append("这个用户关注者低 (").append(user.getFollowersCount()).append(")  :)");

        try {

            Relationship ship = api.showFriendship(user.getId(),917716145121009664L);

            if (ship.isTargetFollowingSource() && ship.isTargetFollowedBySource()) {

                status.append("这个用户与 ").append(link).append(" 互相关注 是萌萌的二次元 :)\n");

            } else if (ship.isSourceFollowingTarget()) {

                status.append("这个用户关注了 ").append(link).append(" :)\n");

            } else if (ship.isSourceFollowedByTarget()) {

                status.append("这个用户被 ").append(link).append(" 关注 是萌萌的二次元").append(" :)\n");

            }

        } catch (TwitterException e) {}

        String statusR = status.toString();

        if (statusR.endsWith("\n")) {

            statusR.substring(0,statusR.length() - 1);

        }

        return statusR;

    }

    void newFollower(UserData user,Twitter api,long id) {

        try {

            User follower = api.showUser(id);

            UserArchive.saveCache(follower);

            Relationship ship = api.showFriendship(api.getId(),id);

            new Send(user.id,(ship.isSourceFollowingTarget() ? "乃关注的 " : "") + TApi.formatUserNameHtml(follower) + " 关注了你 :)",parseStatus(api,follower)).enableLinkPreview().html().exec();

        } catch (TwitterException e) {

            if (UserArchive.INSTANCE.exists(id)) {

                new Send(user.id,UserArchive.INSTANCE.get(id).getHtmlURL() + " 关注你 , 但是该账号已经不存在了 :(").enableLinkPreview().html().exec();

            } else {

                new Send(user.id,"用户 (" + id + ") 关注了你 , 但是该账号已经不存在了 :(").enableLinkPreview().html().exec();


            }

        }

    }

    void lostFollower(UserData user,Twitter api,long id) {

        try {

            User follower = api.showUser(id);
            UserArchive.saveCache(follower);

            Relationship ship = api.showFriendship(api.getId(),id);

            if (ship.isSourceBlockingTarget()) {

                new Send(user.id,TApi.formatUserNameHtml(follower) + " 取关并屏蔽了你 :)").enableLinkPreview().html().exec();

            } else if (follower.getFriendsCount() == 0) {

                new Send(user.id,TApi.formatUserNameHtml(follower) + " 取关了你，但对方关注人数为空，可能是账号异常 :)").enableLinkPreview().html().exec();

            } else {

                new Send(user.id,TApi.formatUserNameHtml(follower) + " 取关了你 :)").enableLinkPreview().html().exec();

            }

        } catch (TwitterException e) {

            if (UserArchive.INSTANCE.exists(id)) {

                new Send(user.id,UserArchive.INSTANCE.get(id).getHtmlURL() + " 取关了你 , 因为该账号已经不存在了 :(").enableLinkPreview().html().exec();

            } else {

                new Send(user.id,"用户 (" + id + ") 取关了你 , 因为该账号已经不存在了 :(").enableLinkPreview().html().exec();

            }

        }



    }

}
