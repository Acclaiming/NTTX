package io.kurumi.ntt.funcs.twitter.track;

import cn.hutool.core.util.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.request.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.twitter.archive.*;
import io.kurumi.ntt.utils.*;
import java.util.*;
import twitter4j.*;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.not;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;
import static java.util.Arrays.asList;
import com.mongodb.client.*;
import io.kurumi.ntt.funcs.twitter.track.TrackUI.*;
import io.kurumi.ntt.funcs.twitter.track.TrackTask.*;


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
					
					api.verifyCredentials();
                   
					if (setting.followers)
					
                    doTracking(account,setting,api,UserData.get(account.user));
                    
                } catch (TwitterException e) {
                    
                    if (e.getErrorCode() == 89 || e.getErrorCode() == 326) {

                        TrackUI.data.deleteById(setting.id);
						TAuth.data.deleteById(setting.id);
                        
                        new Send(account.user,"对不起，但是因乃的账号已停用 / 冻结 / 被限制 / 取消授权，已移除 (⁎˃ᆺ˂)").exec();

                    } else if (e.getErrorCode() != 130) {

                        BotLog.error("UserArchive ERROR",e);
                        
                   }
                }

            }
            
        }
        
    }
    
    public static void onUserChange(UserArchive archive,String change) {
        
		System.out.println("user ch " + archive.screenName + " : " + change);
		
        if (TrackUI.data.collection.countDocuments(and(eq("_id",archive.id),eq("hideChange",true))) > 0) {
            
            return;
            
        }
        
        FindIterable<IdsList> subFr = friends.collection.find(eq("ids",archive.id));
        FindIterable<IdsList> subFo = followers.collection.find(eq("ids",archive.id));
        
		LinkedList<Long> processed = new LinkedList<>();
		
        for (IdsList sub : subFr) {
            
            TAuth account = TAuth.getById(sub.id);
            
            if (account == null) {
                
                friends.deleteById(sub.id);
                
                continue;
                
            }
			
            if (TrackUI.data.collection.countDocuments(and(eq("_id",sub.id),eq("followingInfo",true))) > 0) {
                
                processChangeSend(archive,account,change);
				
				processed.add(account.id);
				processed.add(account.user);
				
            }
            
        }
        
        for (IdsList sub : subFo) {

			if (processed.contains(sub.id)) continue;
			
            TAuth account = TAuth.getById(sub.id);

            if (account == null) {

                friends.deleteById(sub.id);

                continue;

            }
			
			if (processed.contains(account.user)) continue;

            if (TrackUI.data.collection.countDocuments(and(eq("_id",sub.id),eq("followersInfo",true))) > 0) {
				
                processChangeSend(archive,account,change);
                
            }

        }

    }
	
	static void processChangeSend(UserArchive archive,TAuth account,String change) {
		
		TrackUI.TrackSetting setting = TrackUI.data.getById(account.user);

		if (setting == null || (!setting.followers && !setting.followersInfo && !setting.followingInfo)) {
			
			friends.deleteById(account.id);
			followers.deleteById(account.id);
			
			if (setting != null) TrackUI.data.deleteById(account.id);
			
			return;
			
		}
		
		StringBuilder msg = new StringBuilder(TAuth.data.countByField("user",account.user) > 1 ? account.archive().urlHtml() : " : ");
		
		boolean isfo = followers.fieldEquals(account.id,"ids",archive.id);
		boolean isfr = friends.fieldEquals(account.id,"ids",archive.id);
		
		if (isfo && isfr) msg.append("与乃互关");
			else if (isfo) msg.append("关注乃");
				else msg.append("乃关注");
				
		msg.append("的 ").append(archive.urlHtml()).append(" ( #").append(archive.oldScreenName()).append(" ) :\n").append(change);
		
		System.out.println(archive.screenName + " sended");
		
		new Send(account.user,msg.toString()).html().exec();

	}

    void doTracking(TAuth account,TrackUI.TrackSetting setting,Twitter api,UserData user) throws TwitterException {
       
        List<Long> lostFolowers = followers.containsId(account.id) ? followers.getById(account.id).ids : null;
        List<Long> newFollowers = TApi.getAllFoIDs(api,account.id);

        if (lostFolowers == null) {
            
            followers.setById(account.id,new IdsList(account.id,newFollowers));
            
            return;
            
        } else {
            
            followers.setById(account.id,new IdsList(account.id,newFollowers));
            
        }
        
        if (newFollowers.equals(lostFolowers)) return;
       
        List<Long> retains = new LinkedList<>();
        
        retains.addAll(lostFolowers);
        retains.retainAll(newFollowers);
        
        lostFolowers.removeAll(retains);
        newFollowers.removeAll(retains);
        
        for (Long newfollower : newFollowers) {
            
            newFollower(account.user,api,newfollower);
            
        }
        
        for (Long lostFolower : lostFolowers) {

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

            UserArchive archive = UserArchive.save(follower);

            Relationship ship = api.showFriendship(api.getId(),id);

            new Send(userId,(ship.isSourceFollowingTarget() ? "乃关注的 " : "") + archive.urlHtml() + " 关注了你 :)",parseStatus(api,follower)).buttons(makeOpenButton(archive)).enableLinkPreview().html().exec();

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
            UserArchive archive = UserArchive.save(follower);

            Relationship ship = api.showFriendship(api.getId(),id);

            if (ship.isSourceBlockingTarget()) {

                new Send(userId,"关注乃的 " + archive.urlHtml() + " 屏蔽了你 :)").buttons(makeOpenButton(archive)).enableLinkPreview().html().exec();

            } else {

                new Send(userId,archive.url() + " 取关了你 :)").buttons(makeOpenButton(archive)).enableLinkPreview().html().exec();

            }

        } catch (TwitterException e) {

            if (UserArchive.contains(id)) {

                new Send(userId,"关注乃的 " + UserArchive.get(id).urlHtml() + " 的账号已经不存在了 :(").enableLinkPreview().html().exec();

            } else {

                new Send(userId,"关注你的未记录用户 (" + id + ") 的账号已经不存在了 :(").enableLinkPreview().html().exec();

            }

        }



    }
    
	ButtonMarkup makeOpenButton(final UserArchive target) {
		
		return new ButtonMarkup() {{
			
			newUrlButtonLine(target.name,target.url());
			
			}};
		
	}
    

    static Timer timer;
    
    public static void start() {

        stop();

        timer = new Timer("NTT Twitter Track Task");
        timer.schedule(INSTANCE,new Date(),3 * 60 * 1000);

    }

    public static void stop() {

        if (timer != null) timer.cancel();
        timer = null;

    }

    
    
}
