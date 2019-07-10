package io.kurumi.ntt.fragment.twitter.ext;

import cn.hutool.core.util.ArrayUtil;
import io.kurumi.ntt.db.Data;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.archive.StatusArchive;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.NTT;
import java.util.Date;
import java.util.LinkedList;
import java.util.TimerTask;
import twitter4j.DirectMessage;
import twitter4j.DirectMessageList;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TimelineUI extends Fragment {

    public static Data<TLSetting> data = new Data<TLSetting>(TLSetting.class);
    static long lastTimeline = System.currentTimeMillis();

    public static void start() {

        BotFragment.mainTimer.schedule(new Mention(), new Date());

        BotFragment.mainTimer.scheduleAtFixedRate(new Timeline(), new Date(), 2 * 60 * 1000);

    }

    static void processTimeline(TAuth auth, Twitter api, TLSetting setting) throws TwitterException {

        if (setting.timelineOffset != -1) {

            ResponseList<Status> timeline = api.getHomeTimeline(new Paging().count(200).sinceId(setting.timelineOffset + 1));

            long offset = setting.timelineOffset;

            for (Status status : ArrayUtil.reverse(timeline.toArray(new Status[timeline.size()]))) {

                if (status.getId() > offset) {

                    offset = status.getId();

                }

                StatusArchive archive = StatusArchive.save(status).loop(api);

                if (!archive.from.equals(auth.id)) {

                    archive.sendTo(auth.user, 1, auth, status);

                }

            }

            setting.timelineOffset = offset;

        } else {

            ResponseList<Status> timeline = api.getHomeTimeline(new Paging().count(1));

            if (!timeline.isEmpty()) {

                setting.timelineOffset = timeline.get(0).getId();

            } else {

                setting.timelineOffset = 0;

            }

        }

        data.setById(auth.id, setting);

    }

    static void processMention(TAuth auth, Twitter api, TLSetting setting) throws TwitterException {

        if (setting.mentionOffset != -1) {

            ResponseList<Status> mentions = api.getMentionsTimeline(new Paging().count(200).sinceId(setting.mentionOffset + 1));

            long offset = setting.mentionOffset;

            for (Status mention : ArrayUtil.reverse(mentions.toArray(new Status[mentions.size()]))) {

                if (mention.getId() > offset) {

                    offset = mention.getId();

                }

                StatusArchive archive = StatusArchive.save(mention).loop(api);

                if (!archive.from.equals(auth.id)) {

                    archive.sendTo(auth.user, 1, auth, mention);

                }

            }

            setting.mentionOffset = offset;

        } else {

            ResponseList<Status> mention = api.getMentionsTimeline(new Paging().count(1));

            if (mention.isEmpty()) {

                setting.mentionOffset = 0;

            } else {

                setting.mentionOffset = mention.get(0).getId();

            }

        }

        if (setting.retweetsOffset != -1) {

            ResponseList<Status> retweets = api.getRetweetsOfMe(new Paging().count(200).sinceId(setting.retweetsOffset + 1));

            long offset = setting.retweetsOffset;

            for (Status retweet : ArrayUtil.reverse(retweets.toArray(new Status[retweets.size()]))) {

                if (retweet.getId() > offset) {

                    offset = retweet.getId();

                }

                StatusArchive archive = StatusArchive.save(retweet).loop(api);

                if (!archive.from.equals(auth.id)) {

                    archive.sendTo(auth.user, 1, auth, retweet);

                }

            }

            setting.retweetsOffset = offset;

        } else {

            ResponseList<Status> mention = api.getRetweetsOfMe(new Paging().count(1));

            if (!mention.isEmpty()) {

                setting.retweetsOffset = mention.get(0).getId();

            } else {

                setting.retweetsOffset = 0;

            }

        }

        data.setById(auth.id, setting);

    }
    
    static void processDM(TAuth auth, Twitter api, TLSetting setting) throws TwitterException {

        if (setting.directMessageOffset != -1) {

            DirectMessageList dms = api.getDirectMessages(50);

            long offset = setting.mentionOffset;

            for (DirectMessage dm : ArrayUtil.reverse(dms.toArray(new DirectMessage[dms.size()]))) {

                if (dm.getId() > offset) {

                    offset = dm.getId();

                }
                
                if (!auth.id.equals(dm.getSenderId())) {
                    
                    
                    
                }

            }

            setting.directMessageOffset = offset;

        } else {

            ResponseList<Status> mention = api.getMentionsTimeline(new Paging().count(1));

            if (mention.isEmpty()) {

                setting.mentionOffset = 0;

            } else {

                setting.mentionOffset = mention.get(0).getId();

            }

        }

        if (setting.retweetsOffset != -1) {

            ResponseList<Status> retweets = api.getRetweetsOfMe(new Paging().count(200).sinceId(setting.retweetsOffset + 1));

            long offset = setting.retweetsOffset;

            for (Status retweet : ArrayUtil.reverse(retweets.toArray(new Status[retweets.size()]))) {

                if (retweet.getId() > offset) {

                    offset = retweet.getId();

                }

                StatusArchive archive = StatusArchive.save(retweet).loop(api);

                if (!archive.from.equals(auth.id)) {

                    archive.sendTo(auth.user, 1, auth, retweet);

                }

            }

            setting.retweetsOffset = offset;

        } else {

            ResponseList<Status> mention = api.getRetweetsOfMe(new Paging().count(1));

            if (!mention.isEmpty()) {

                setting.retweetsOffset = mention.get(0).getId();

            } else {

                setting.retweetsOffset = 0;

            }

        }

        data.setById(auth.id, setting);
        
    }

	@Override
	public void init(BotFragment origin) {
		
		super.init(origin);

		registerFunction("timeline","mention","dm");

    }

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
		requestTwitter(user,msg);
		
	}
	

    @Override
    public void onTwitterFunction(UserData user, Msg msg, String function, String[] params, TAuth account) {

        TLSetting setting = data.getById(account.id);

        if (setting == null) {

            setting = new TLSetting();

            setting.id = account.id;

        }

        boolean target = params.length > 0 && !"off".equals(params[0]);

        boolean old = target;

        if ("dm".equals(function)) {

            old = setting.directMessages;

            setting.directMessages = target;

        } else if ("timeline".equals(function)) {
            
            old = setting.timeline;
            
            setting.timeline = target;
            
        } else {
            
            old = setting.mention;
            
            setting.mention =target;
            
        }
        
        msg.send(old == target ? (target ? "无须重复开启" : "没有开启") : target ? "已开启" : "已关闭").exec();

		if (!target) {

            if ("dm".equals(function)) {
                
                setting.directMessageOffset = -1;
                
			} else if ("timeline".equals(function)) {

				setting.timelineOffset = -1;

			} else {

				setting.retweetsOffset = -1;
				setting.mentionOffset = -1;

			}

		}

        if (setting.mention || setting.timeline || setting.directMessages) {

            data.setById(account.id, setting);

        } else {

            data.deleteById(account.id);

        }

    }

    public static class TLSetting {

        public long id;

        public boolean timeline;
        public long timelineOffset = -1;

        public boolean mention = false;

        public long mentionOffset = -1;
        public long retweetsOffset = -1;

        public boolean directMessages = false;
        public long directMessageOffset = -1;

    }

    public static class Timeline extends TimerTask {

        @Override
        public void run() {

            LinkedList<Long> toRemove = new LinkedList<>();

            for (final TLSetting setting : data.collection.find()) {

                final TAuth auth = TAuth.getById(setting.id);

                if (auth == null) {

                    toRemove.add(setting.id);

                    continue;

                }

                final Twitter api = auth.createApi();

                if (setting.timeline) {

                    execute(new Runnable() {

							@Override
							public void run() {

								try {

									processTimeline(auth, api, setting);

								} catch (TwitterException e) {

									if (e.getStatusCode() == 503 || e.getErrorCode() == -1) return;

									setting.timeline = false;

									new Send(auth.user, "时间流已关闭 :", NTT.parseTwitterException(e)).exec();

									data.setById(auth.id, setting);

								}

							}

						});

                }

            }

            for (long remove : toRemove) {

                data.deleteById(remove);

            }

        }


    }

    public static class Mention extends TimerTask {

        @Override
        public void run() {

            LinkedList<Long> toRemove = new LinkedList<>();

            for (final TLSetting setting : data.collection.find()) {

                final TAuth auth = TAuth.getById(setting.id);

                if (auth == null) {

                    toRemove.add(setting.id);

                    continue;

                }

                final Twitter api = auth.createApi();

                if (setting.mention) {

                    execute(new Runnable() {

							@Override
							public void run() {

								try {

									processMention(auth, api, setting);

								} catch (TwitterException e) {

									if (e.getStatusCode() == 503 || e.getErrorCode() == -1) return;
									
									setting.mention = false;

									new Send(auth.user, "回复流已关闭 :", NTT.parseTwitterException(e)).exec();

									data.setById(auth.id, setting);

								}

							}

						});

                }

            }

            for (long remove : toRemove) {

                data.deleteById(remove);

            }

            long users = data.countByField("mention", true);

            long delay = ((users / (100000 / 24 / 60))) * 60 * 1000 + 30 * 1000;

            if (System.currentTimeMillis() < 1560873571200L) {

                // utc 2019 06 19 Twitter将mention_timeline限制为每天 10w次总共调用。

                delay = 20 * 1000;

            }

            BotFragment.mainTimer.schedule(new Mention(), new Date(System.currentTimeMillis() + delay));

        }

    }

}
