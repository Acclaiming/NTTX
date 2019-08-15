package io.kurumi.ntt.fragment.twitter.tasks;

import cn.hutool.core.util.ArrayUtil;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.archive.StatusArchive;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.NTT;
import java.util.TimerTask;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import java.util.HashMap;
import io.kurumi.ntt.fragment.twitter.bots.MDListener;

public class MentionTask extends TimerTask {

	@Override
	public void run() {

		for (TAuth account : TAuth.data.getAll()) {

			// if (account.mention == null && account.mdb == null) continue;

			final Twitter api = account.createApi();

			try {

				processMention(account,api);

			} catch (TwitterException e) {

				if (account.mention == null && account.mdb == null) continue;
				
				if (e.getStatusCode() == 503 || e.getErrorCode() == -1 || e.getStatusCode() == 429) return;

				account.mention = null;

				new Send(account.user,"回复流已关闭 :",NTT.parseTwitterException(e)).exec();

				if (TAuth.data.containsId(account.id)) {

					TAuth.data.setById(account.id,account);

				}

			}

		}

	}

	static HashMap<Long,MDListener> bots = new HashMap<>();

	static void processMention(TAuth auth,Twitter api) throws TwitterException {

		long offset = -1;

        if (auth.mention_offset != null) {

            ResponseList<Status> mentions = api.getMentionsTimeline(new Paging().count(200).sinceId(auth.mention_offset + 1));

            offset = auth.mention_offset;

            for (Status mention : ArrayUtil.reverse(mentions.toArray(new Status[mentions.size()]))) {

                if (mention.getId() > offset) {

                    offset = mention.getId();

                }

                StatusArchive archive = StatusArchive.save(mention).loop(api);

                if (auth.mention != null && !archive.from.equals(auth.id)) {

                    archive.sendTo(auth.user,1,auth,mention);

                }

				if (auth.mdb != null) { 	

					if (bots.containsKey(auth.id)) {

						bots.get(auth.id).onStatus(mention);

					} else {

						MDListener bot = new MDListener(auth);

						bots.put(auth.id,bot);

						bot.onStatus(mention);

					}

				}

            }

        } else {

            ResponseList<Status> mention = api.getMentionsTimeline(new Paging().count(1));

            if (mention.isEmpty()) {

				offset = 0;

            } else {

                offset = mention.get(0).getId();

            }

        }

		long rt_offset = 0;

        if (auth.rt_offset != null) {

			rt_offset = auth.rt_offset;

            ResponseList<Status> retweets = api.getRetweetsOfMe(new Paging().count(200).sinceId(rt_offset + 1));

            for (Status retweet : ArrayUtil.reverse(retweets.toArray(new Status[retweets.size()]))) {

                if (retweet.getId() > offset) {

                    offset = retweet.getId();

                }

                StatusArchive archive = StatusArchive.save(retweet).loop(api);

                if (!archive.from.equals(auth.id)) {

                    archive.sendTo(auth.user,1,auth,retweet);

                }

            }

        } else {

            ResponseList<Status> mention = api.getRetweetsOfMe(new Paging().count(1));

            if (!mention.isEmpty()) {

                rt_offset = mention.get(0).getId();

            } else {

                rt_offset = 0;

            }

        }

        if (auth.mention_offset == null || !auth.mention_offset.equals(offset) || auth.rt_offset == null || !auth.rt_offset.equals(rt_offset)) {

			auth.mention_offset = offset;
			auth.rt_offset = rt_offset;
			
			TAuth.data.setById(auth.id,auth);

		}

    }


}
