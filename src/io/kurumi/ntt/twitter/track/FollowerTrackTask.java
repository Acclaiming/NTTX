package io.kurumi.ntt.twitter.track;

import cn.hutool.json.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.request.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.twitter.archive.*;
import io.kurumi.ntt.utils.*;
import java.util.*;
import twitter4j.*;

import cn.hutool.json.JSONObject;

public class FollowerTrackTask extends TimerTask {

    static FollowerTrackTask INSTANCE = new FollowerTrackTask();
    static Timer timer;
    public static JSONObject enable = BotDB.getJSON("data","track",true);
    static HashMap<Long,LinkedList<Long>> cache = new HashMap<>();
    
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

            UserData user = UserData.INSTANCE.get(userId);

            if (!TAuth.exists(user)) {

                enable.remove(user.idStr);
                
                BotDB.setJSONArray("cache","track/" + user.idStr,null);
                
                save();

                continue;

            }

            Twitter api = TAuth.get(user).createApi();
            
            try {
                
                LinkedList<Long> last = cache.containsKey(api.getId()) ? cache.get(api.getId()) : null;

                LinkedList<Long> latest = TApi.getAllFoIDs(api,api.getId());

                cache.put(api.getId(),latest);
                
                for (long id : latest) {
                    
                    if (last != null) {

                        if (!last.remove(id)) {

                            newFollower(user,api.showUser(id));

                        } 

                    }

                }

                if (last != null && last.size() > 0) {

                    for (int index = 0;index < last.size();index ++) {

                        long id = last.get(index);

                        lostFollower(user,api,id);

                    }

                }

                
            } catch (TwitterException e) {
                
                BotLog.info("UserArchive ERROR",e);
                
            }

        }

    }

    void newFollower(UserData user,User follower) {

		UserArchive fa = UserArchive.INSTANCE.getOrNew(follower.getId());

		fa.read(follower);

		UserArchive.INSTANCE.saveObj(fa);
		
        new Send(user.id,TApi.formatUserNameHtml(follower) + " 关注了你").enableLinkPreview().html().exec();

    }

    void lostFollower(UserData user,Twitter api,long id) {

        try {

            User follower = api.showUser(id);
			
			UserArchive fa = UserArchive.INSTANCE.getOrNew(id);

			fa.read(follower);
			
			UserArchive.INSTANCE.saveObj(fa);
			
            Relationship ship = api.showFriendship(api.getId(),id);

            if (ship.isSourceBlockingTarget()) {

                new Send(user.id,TApi.formatUserNameHtml(follower) + " 取关并屏蔽了你 :(").enableLinkPreview().html().exec();
                
            } else if (follower.getFriendsCount() == 0) {

                new Send(user.id,TApi.formatUserNameHtml(follower) + " 取关了你，但对方关注人数为空，可能是账号异常 :(").enableLinkPreview().html().exec();

            } else {

                new Send(user.id,TApi.formatUserNameHtml(follower) + " 取关了你 :(").enableLinkPreview().html().exec();

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
