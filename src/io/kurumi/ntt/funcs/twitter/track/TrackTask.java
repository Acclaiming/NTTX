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
import io.kurumi.ntt.*;
import com.pengrad.telegrambot.request.*;
import cn.hutool.http.*;
import java.io.*;
import com.pengrad.telegrambot.model.request.*;
import io.kurumi.ntt.fragment.twitter.auto.*;
import cn.hutool.core.io.*;
import com.pengrad.telegrambot.response.*;
import io.kurumi.ntt.fragment.twitter.status.*;
import java.time.chrono.*;
import com.google.gson.internal.bind.util.*;
import com.neovisionaries.i18n.*;


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

		LinkedList<TAuth> remove = new LinkedList<>();

        for (TAuth account : TAuth.data.collection.find()) {

			TrackUI.TrackSetting setting = TrackUI.data.getById(account.id);

			if (setting == null) setting = new TrackUI.TrackSetting();

			Twitter api =  account.createApi();

			try {

				api.verifyCredentials();

				//if (setting.followers || setting.followersInfo || setting.followingInfo) {

				doTracking(account,setting,api,UserData.get(account.user));

				//}

			} catch (TwitterException e) {

				if (e.getErrorCode() == 89 || e.getErrorCode() == 215 || e.getErrorCode() == 215 || e.getErrorCode() == 326) {

					remove.add(account);

				} else if (e.getErrorCode() == 326) {
					
					// 被限制;

				} else if (e.getErrorCode() != 130) {

					BotLog.error("UserArchive ERROR",e);

				}
			}


		}

		for (TAuth account : remove) {

			TrackUI.data.deleteById(account.id);
			TAuth.data.deleteById(account.id);

			new Send(account.user,"对不起，但是因乃的账号已停用 / 冻结 / NTT被取消授权，已移除 (⁎˃ᆺ˂)").exec();

		}


    }

    public static void onUserChange(UserArchive archive,String change) {

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

			TrackUI.TrackSetting setting = TrackUI.data.getById(account.id);

			if (setting == null || (!setting.followers && !setting.followersInfo && !setting.followingInfo)) {

				friends.deleteById(account.id);
				followers.deleteById(account.id);

				if (setting != null) TrackUI.data.deleteById(account.id);

				return;

			}

            if (setting.followingInfo) {

                processChangeSend(archive,account,change,setting);

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

			System.out.println("sub : " + account.archive().name);

			TrackUI.TrackSetting setting = TrackUI.data.getById(account.id);

			if (setting == null || (!setting.followers && !setting.followersInfo && !setting.followingInfo)) {

				friends.deleteById(account.id);
				followers.deleteById(account.id);

				if (setting != null) TrackUI.data.deleteById(account.id);

				return;

			}

			if (processed.contains(account.user)) continue;

            if (setting.followersInfo) {

                processChangeSend(archive,account,change,setting);

				processed.add(account.id);
				processed.add(account.user);

            }

        }

    }

	static void processChangeSend(UserArchive archive,TAuth account,String change,TrackUI.TrackSetting setting) {


		StringBuilder msg = new StringBuilder(TAuth.data.countByField("user",account.user) > 1 ? account.archive().urlHtml() + " : " : "");

		boolean isfo = followers.fieldEquals(account.id,"ids",archive.id);
		boolean isfr = friends.fieldEquals(account.id,"ids",archive.id);

		if (isfo && isfr) msg.append("与乃互关");
		else if (isfo) msg.append("关注乃");
		else msg.append("乃关注");

		msg.append("的 ").append(archive.urlHtml()).append(" ( #").append(archive.oldScreenName()).append(" ) :\n").append(change);


		if (archive.oldPhotoUrl == null && (archive.oldBannerUrl == null || archive.bannerUrl == null)) {

			new Send(account.user,msg.toString()).html().point(0,archive.id);

		} else if (archive.oldPhotoUrl != null) {

			File photo = new File(Env.CACHE_DIR,"twitter_profile_images/" + FileUtil.getName(archive.photoUrl));

			if (!photo.isFile()) {

				HttpUtil.downloadFile(archive.photoUrl,photo);

			}

			SendResponse resp = Launcher.INSTANCE.bot().execute(new SendPhoto(account.user,photo).caption(msg.toString()).parseMode(ParseMode.HTML));

			if (resp.isOk()) {

				MessagePoint.set(resp.message().messageId(),0,archive.id);

			}

		} else {

			File photo = new File(Env.CACHE_DIR,"twitter_banner_images/" + archive.id + "/" + System.currentTimeMillis() + ".jpg");

			if (!photo.isFile()) {

				HttpUtil.downloadFile(archive.bannerUrl,photo);

			}

			SendResponse resp = Launcher.INSTANCE.bot().execute(new SendPhoto(account.user,photo).caption(msg.toString()).parseMode(ParseMode.HTML));

			if (resp.isOk()) {

				MessagePoint.set(resp.message().messageId(),0,archive.id);

			}

		}

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

		friends.setById(account.id,new IdsList(account.id,TApi.getAllFrIDs(api,account.id)));

		List<Long> retains = new LinkedList<>();

        if (!newFollowers.equals(lostFolowers)) {

			retains.addAll(lostFolowers);
			retains.retainAll(newFollowers);

			lostFolowers.removeAll(retains);
			newFollowers.removeAll(retains);

			for (Long newfollower : newFollowers) {

				newFollower(account,api,newfollower,setting.followers);

			}

			for (Long lostFolower : lostFolowers) {

				lostFollower(account,api,lostFolower,setting.followers);

			}

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

		if (user.getWithheldInCountries() != null) {
			
			status.append("警告 : 此账号违反了");
			
			for (String countryCode : user.getWithheldInCountries()) {
				
				CountryCode country = CountryCode.getByAlpha2Code(countryCode);

				status.append(country.toLocale().toString()).append(" ");
				
			}
			
			status.append(" 的当地法律 被Twitter标识");
			
		}
		
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

    void newFollower(TAuth auth,Twitter api,long id,boolean notice) {

        try {

            User follower = api.showUser(id);

            UserArchive archive = UserArchive.save(follower);

            Relationship ship = api.showFriendship(auth.id,id);

			if (notice) {

				new Send(auth.user,(ship.isSourceFollowingTarget() ? "已关注的 " : "") + archive.urlHtml() + " #" + archive.screenName + " 关注了你 :)",parseStatus(api,follower)).html().point(0,archive.id);

			}

			AutoTask.onNewFollower(auth,api,archive,ship);

        } catch (TwitterException e) {

			if (!notice) return;

            if (UserArchive.contains(id)) {

				UserArchive archive = UserArchive.get(id);

                new Send(auth.user,archive.urlHtml() + " #" + archive.screenName + " 关注了你 , 但是该账号已经不存在了 :(").html().point(0,archive.id);

            } else {

                new Send(auth.user,"用户 (" + id + ") 关注了你 , 但是该账号已经不存在了 :(").html().point(0,id);

            }

        }

    }

    void lostFollower(TAuth auth,Twitter api,long id,boolean notice) {

        try {

            User follower = api.showUser(id);
            UserArchive archive = UserArchive.save(follower);

            Relationship ship = api.showFriendship(id,auth.id);

			if (notice) {

				if (ship.isSourceBlockingTarget()) {

					new Send(auth.user,archive.urlHtml() + " #" + archive.screenName + " 取关并屏蔽了你 :)").html().point(0,archive.id);

				} else {

					new Send(auth.user,archive.urlHtml() + " #" + archive.screenName + " 取关了你 :)").html().point(0,archive.id);

				}

			}

        } catch (TwitterException e) {

			if (!notice) return;

            if (UserArchive.contains(id)) {

				UserArchive archive = UserArchive.get(id);

                new Send(auth.user,"关注者 " + archive.urlHtml() + " #" + archive.screenName + " 的账号已经不存在了 :(").html().point(0,archive.id);

            } else {

                new Send(auth.user,"无记录的关注者 (" + id + ") 的账号已经不存在了 :(").html().point(0,id);

            }

        }



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
