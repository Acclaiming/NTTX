package io.kurumi.ntt.twitter.track;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import io.kurumi.ntt.Launcher;
import io.kurumi.ntt.db.BotDB;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.twitter.TApi;
import io.kurumi.ntt.twitter.TAuth;
import io.kurumi.ntt.twitter.archive.UserArchive;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class TrackTask extends TimerTask {

    public static TrackTask INSTANCE = new TrackTask();
    Timer timer = new Timer("NTT Twitter Track Task");
    public JSONObject enable = BotDB.getJSON("data","track",true);

    public void start() {

        timer.scheduleAtFixedRate(this,new Date(),5 * 60 * 1000);

    }

    public void stop() {

        timer.cancel();

    }

   public void save() {

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

            TAuth auth = TAuth.get(user);
            Twitter api = auth.createApi();

            JSONArray last = BotDB.getJSONArray("cache","track/" + user.idStr,false);
            
            JSONArray latest = new JSONArray();
            
            try {

                long[] ids = TApi.getAllFoIDs(api,auth.accountId);


                for (long id : ids) {
                    
                    latest.add(id);

                    if (last != null) {

                        if (!last.remove(id)) {

                            newFollower(user,api.showUser(id));

                        } 

                    }

                }

                if (last != null && last.size() > 0) {

                    for (int index = 0;index < last.size();index ++) {

                        long id = last.getLong(index);

                        lostFollower(user,api,id);

                    }

                }

                BotDB.setJSONArray("cache","track/" + user.idStr,latest);
                
            } catch (TwitterException e) {
                
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                e.printStackTrace(new PrintWriter(out,true));
                
                new Send(Launcher.INSTANCE,user.id,StrUtil.str(out.toByteArray(),CharsetUtil.CHARSET_UTF_8)).exec();
                
            }

        }

    }

    void newFollower(UserData user,User follower) {

        new Send(Launcher.INSTANCE,user.id,TApi.formatUserNameHtml(follower) + " 关注了你").enableLinkPreview().html().exec();

    }

    void lostFollower(UserData user,Twitter api,long id) {

        try {

            User follower = api.showUser(id);
            Relationship ship = api.showFriendship(api.getId(),id);

            if (ship.isSourceBlockingTarget()) {

                new Send(Launcher.INSTANCE,TApi.formatUserNameHtml(follower) + " 取关并屏蔽了你 :(").enableLinkPreview().html().exec();

            } else if (follower.getFriendsCount() == 0) {

                new Send(Launcher.INSTANCE,TApi.formatUserNameHtml(follower) + " 取关了你，但对方关注人数为空，可能是账号异常 :(").enableLinkPreview().html().exec();

            } else {

                new Send(Launcher.INSTANCE,TApi.formatUserNameHtml(follower) + " 取关了你 :(").enableLinkPreview().html().exec();

            }

        } catch (TwitterException e) {

            if (UserArchive.INSTANCE.exists(id)) {

                new Send(Launcher.INSTANCE,UserArchive.INSTANCE.get(id).getHtmlURL() + " 取关了你 , 因为该账号已经不存在了 :(").enableLinkPreview().html().exec();

            } else {

                new Send(Launcher.INSTANCE,"用户 (" + id + ") 取关了你 , 因为该账号已经不存在了 :(").enableLinkPreview().html().exec();


            }



        }



    }

}
