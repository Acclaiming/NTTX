package io.kurumi.ntt.funcs.twitter.track;

import cn.hutool.core.util.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.request.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.twitter.archive.*;
import io.kurumi.ntt.utils.*;
import java.util.*;
import twitter4j.*;

public class TrackTask extends TimerTask {

    public static TrackTask INSTANCE = new TrackTask();
    
    public static class IdsList {

        public Long user;
        
        public Long id;
        public List<Long> ids;

        public IdsList() {}

        public IdsList(Long id,List<Long> ids) {

            this.id = id;
            this.ids = ids;

        }

    }
    
    public static Data<IdsList> followers = new Data<IdsList>("Followers",IdsList.class);
    public static Data<IdsList> friends = new Data<IdsList>("Friends",IdsList.class);
    
    @Override
    public void run() {
        
        for (TrackUI.TrackSetting setting : TrackUI.data.collection.find()) {
            
            TAuth account = TAuth.getById(setting.id);

            if (account != null) {
                
                Twitter api =  account.createApi();
                
                try {
                    
                    doTracking(account,setting,api,UserData.get(account.user));
                    
                } catch (TwitterException e) {
                    
                    if (e.getErrorCode() == 89 || e.getErrorCode() == 326) {

                        TrackUI.data.deleteById(setting.id);
                        
                        new Send(account.user,"对不起，但是因乃的账号已被Twitter限制，已经自动关闭所有监听 (需要登录 twitter.com 以解除限制并重新开启 (⁎˃ᆺ˂)").exec();

                    } else if (e.getErrorCode() != 130) {

                        BotLog.error("UserArchive ERROR",e);
                        
                   }
                }

            }
            
        }
        
    }

    void doTracking(TAuth account,TrackUI.TrackSetting setting,Twitter api,UserData user) throws TwitterException {
       
        List<Long> lostFolowers = followers.containsId(account.id) ? followers.getById(account.id).ids : null;
        List<Long> newFollowers = TApi.getAllFoIDs(api,account.id);

        if (lostFolowers == null) {
            
            followers.setById(account.id,new IdsList(account.id,newFollowers));
            
            return;
            
        }
       
        List<Long> retains = new LinkedList<>();
        
        retains.addAll(lostFolowers);
        retains.retainAll(newFollowers);
        
        lostFolowers.removeAll(retains);
        newFollowers.removeAll(retains);
        
        for (Long newfollower : newFollowers) {
            
            newFollower(account.user,api,newfollower);
            
        }
        
        for (Long lostFolower : newFollowers) {

            lostFollower(account.user,api,lostFolower);

        }
        
        while (!retains.isEmpty()) {
            
            List<Long> target;
            
            if (retains.size() > 100) {

                target = retains.subList(0,100);

                retains = new LinkedList<Long>(retains.subList(99,retains.size()));


            } else {

                target = new LinkedList<>();
                target.addAll(retains);

                retains.clear();

            }
            
            try {

                ResponseList<User> result = api.lookupUsers(ArrayUtil.unWrap(target.toArray(new Long[target.size()])));

                for (User tuser : result) {

                    target.remove(tuser.getId());

                    UserArchive.save(tuser);

                }

                for (Long da : target) {

                    UserArchive.saveDisappeared(da);

                }

            } catch (TwitterException e) {

                if (e.getErrorCode() == 17) {

                    for (Long da : target) {

                        UserArchive.saveDisappeared(da);

                    }

                }
                
            }
            
            
        }

    }
    
    String parseStatus(Twitter api,User user) {

        StringBuilder status = new StringBuilder();

        try {

            if (!api.showFriendship(api.getId(),user.getId()).isSourceFollowingTarget() && !user.isFollowRequestSent()) {

                if (user.isProtected()) status.append("这是一个是锁推用户 :)\n");

            }

        } catch (TwitterException e) {}

        // if (user.isFollowRequestSent()) status.append("乃发送了关注请求 :)\n");
        if (user.getStatusesCount() == 0) status.append("这个用户没有发过推 :)\n");
        if (user.getFavouritesCount() == 0) status.append("这个用户没有喜欢过推文 :)\n");
        if (user.getFollowersCount() < 20) status.append("这个用户关注者低 (").append(user.getFollowersCount()).append(")  :)\n");

        /*

         try {

         Relationship ship = api.showFriendship(user.getId(),917716145121009664L);

         if (ship.isTargetFollowingSource() && ship.isTargetFollowedBySource()) {

         status.append("这个用户与 ").append(link).append(" 互相关注 是萌萌的二次元 :)\n");

         } else if (ship.isSourceFollowingTarget()) {

         status.append("这个用户关注了 ").append(link).append(" :)\n");

         } else if (ship.isSourceFollowedByTarget()) {

         status.append("这个用户被 ").append(link).append(" 关注 是萌萌的二次元 :)\n");

         }

         } catch (TwitterException e) {}

         */
         
        
        String statusR = status.toString();

        if (statusR.endsWith("\n")) {

            statusR.substring(0,statusR.length() - 1);

        }

        return statusR;

    }

    void newFollower(Long userId,Twitter api,long id) {

        try {

            User follower = api.showUser(id);

            UserArchive.save(follower);

            Relationship ship = api.showFriendship(api.getId(),id);

            new Send(userId,(ship.isSourceFollowingTarget() ? "乃关注的 " : "") + TApi.formatUserNameHtml(follower) + " 关注了你 :)",parseStatus(api,follower)).enableLinkPreview().html().exec();

        } catch (TwitterException e) {

            if (UserArchive.contains(id)) {

                new Send(userId,UserArchive.get(id).urlHtml() + " 关注你 , 但是该账号已经不存在了 :(").enableLinkPreview().html().exec();

            } else {

                new Send(userId,"用户 (" + id + ") 关注了你 , 但是该账号已经不存在了 :(").enableLinkPreview().html().exec();


            }

        }

    }

    void lostFollower(Long userId,Twitter api,long id) {

        try {

            User follower = api.showUser(id);
            UserArchive.save(follower);

            Relationship ship = api.showFriendship(api.getId(),id);

            if (ship.isSourceBlockingTarget()) {

                new Send(userId,"关注乃的 " + TApi.formatUserNameHtml(follower) + " 屏蔽了你 :)").enableLinkPreview().html().exec();

            } else if (follower.getFriendsCount() == 0) {

                new Send(userId,TApi.formatUserNameHtml(follower) + " 取关了你，且关注人数为空 :)").enableLinkPreview().html().exec();

            } else {

                new Send(userId,TApi.formatUserNameHtml(follower) + " 取关了你 :)").enableLinkPreview().html().exec();

            }

        } catch (TwitterException e) {

            if (UserArchive.contains(id)) {

                new Send(userId,"关注乃的 " + UserArchive.get(id).urlHtml() + " 的账号已经不存在了 :(").enableLinkPreview().html().exec();

            } else {

                new Send(userId,"关注你的未记录用户 (" + id + ") 的账号已经不存在了 :(").enableLinkPreview().html().exec();

            }

        }



    }
    
    

    static Timer timer;
    
    public static void start() {

        stop();

        timer = new Timer("NTT Twitter Track Task");
        timer.schedule(INSTANCE,new Date(),15 * 60 * 1000);

    }

    public static void stop() {

        if (timer != null) timer.cancel();
        timer = null;

    }

    
    
}
