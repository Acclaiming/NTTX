package io.kurumi.ntt.fragment.twitter.auto;

import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.archive.UserArchive;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.NTT;
import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class AutoTask {
	
	/*
	
	public static void start() {

		stop();

		timer.scheduleAtFixedRate(INSTANCE,new Date(),5 * 60 * 1000);

	}

	public static void stop() {

		if (timer != null) {

			timer.cancel();

			timer = null;

		}

	}
	
	*/
	
	public static void onNewFriend(TAuth auth, Twitter api, UserArchive archive, Relationship ship) {

        if (ship.isSourceWantRetweets() && AutoUI.autoData.fieldEquals(auth.id, "mrt", true)) {

            try {

                api.updateFriendship(archive.id,ship.isSourceNotificationsEnabled(),false);

            } catch (TwitterException e) {
            }

        }

    }

    public static void onNewFollower(TAuth auth, Twitter api, UserArchive archive, Relationship ship) {

        if (!ship.isSourceFollowingTarget() && AutoUI.autoData.fieldEquals(auth.id, "foback", true)) {

            try {

                api.createFriendship(archive.id);

                new Send(auth.user, (archive.isProtected ? "已发送关注请求 :" : "已回Fo : ") + archive.urlHtml(), "账号 : " + auth.archive().urlHtml()).html().point(0, archive.id);

            } catch (TwitterException e) {

                new Send(auth.user, "回Fo失败 : " + archive.urlHtml(), NTT.parseTwitterException(e), "账号 : " + auth.archive().urlHtml()).html().exec();


            }

        }

    }
	
	/*

	@Override
	public void run() {

		for (TAuth auth :  TAuth.data.collection.find()) {

			AutoUI.AutoSetting auto = AutoUI.autoData.getById(auth.id);

			

			 if (auth == null) {

			 AutoUI.autoData.deleteById(auto.id);

			 BotLog.debug("autotask removed for " + auto.id);

			 return;

			 }
			 
			 
			 if (auto == null) auto = new AutoUI.AutoSetting();

			try {

				startLikeService(auth,auto);

			} catch (TwitterException e) {
			}

		}

	}

	static LinkedHashSet<Long> saved = new LinkedHashSet<>();

	void startService(TAuth auth,AutoUI.AutoSetting auto) throws TwitterException {

		Twitter api = auth.createApi();

		ResponseList<Status> tl = api.getHomeTimeline(new Paging().count(800));

		int count = 0;
		
		for (Status status : tl) {

			if (auto.archive && !saved.contains(status.getId())) {

				StatusArchive.save(status,api);

				saved.add(status.getId());

			}

			if (saved.size() > 10000) {

				saved.clear();

			}

		
		}

		if (count > 0) {

			//	new Send(auth.user,"sended " + count + " likes to home_timeline","account : " + Html.a("@" + auth.archive().screenName,"https://twitter.com/" + auth.archive().screenName)).html().exec();

		}

	}

	int loopLike(TAuth auth,Twitter api,Status status) throws TwitterException {

		int like = 0;

		if (status.isFavorited()) return 0;

		if (!auth.id.equals(status.getUser().getId())) {

			try {

				api.createFavorite(status.getId());

				like ++;

			} catch (TwitterException e) {

				throw e;

			}

		}

		if (status.getInReplyToStatusId() != -1) {

			try {

				like += loopLike(auth,api,api.showStatus(status.getInReplyToStatusId()));

			} catch (TwitterException e) {

				try {

					api.createFavorite(status.getInReplyToStatusId());

				} catch (TwitterException ex) {}

			}

		}

		if (status.getQuotedStatusId() != -1) {

			try {

				if (status.getQuotedStatus() != null) {

					like += loopLike(auth,api,status.getQuotedStatus());

				} else {

					api.createFavorite(status.getQuotedStatusId());

					like ++;

				}

			} catch (TwitterException e) {}


		}

		return like;


	}
	
	*/

}
