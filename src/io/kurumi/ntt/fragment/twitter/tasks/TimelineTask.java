package io.kurumi.ntt.fragment.twitter.tasks;

import cn.hutool.core.util.ArrayUtil;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.archive.StatusArchive;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.NTT;
import java.util.LinkedList;
import java.util.TimerTask;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import io.kurumi.ntt.fragment.group.MaliciousMessage;
import cn.hutool.core.util.ReUtil;

public class TimelineTask extends TimerTask {

	@Override
	public void run() {

		for (TAuth account : TAuth.data.getAll()) {

			// if (account.tl == null) continue;

			final Twitter api = account.createApi();

			try {

				processTimeline(account,api);

			} catch (TwitterException e) {

				if (account.tl == null) continue;
				
				if (e.getStatusCode() == 503 || e.getErrorCode() == -1 || e.getStatusCode() == 429) return;

				account.tl = null;

				new Send(account.user,"时间流已关闭 :",NTT.parseTwitterException(e)).exec();

				if (TAuth.data.containsId(account.id)) {

					TAuth.data.setById(account.id,account);

				}

			}

		}

	}

    void processTimeline(TAuth auth,Twitter api) throws TwitterException {

		long offset = 0;

        if (auth.tl_offset != null) {

			offset = auth.tl_offset;

            ResponseList<Status> timeline = api.getHomeTimeline(new Paging().count(200).sinceId(offset + 1));

            for (Status status : ArrayUtil.reverse(timeline.toArray(new Status[timeline.size()]))) {

                if (status.getId() > offset) {

                    offset = status.getId();

                }
				
				if (auth.tl_na != null) {
					
					if (status.getSource().contains("IFTTT")) continue;
					
					if (status.getSource().contains("ツイ廃あらーと")) continue;
					
					if (status.getSource().contains("今日のツイライフ")) continue;
					
					if (status.getSource().contains("fllwrs")) continue;
					
					if (status.getSource().contains("twittbot.net")) continue;
					
				}

                StatusArchive archive = StatusArchive.save(status).loop(api);

				if (auth.tl == null) continue;
				
                if (archive.from.equals(auth.id)) continue;

				if (auth.tl_dn != null) {
					
					if (!DeviceNotificationFilter.isDeviceNotificationEnabled(auth,api,status.getUser().getId())) continue;
					
				}
				
				if (archive.retweetedStatus != -1) {

					if (auth.tl_nt != null) continue;

				} else if (archive.inReplyToStatusId == -1) {

					if (auth.tl_ns != null) continue;

				} else {

					if (auth.tl_nr != null) continue;

				}

				if (auth.tl_nesu != null) {

					if (ReUtil.contains(MaliciousMessage.esuWordsRegex,archive.text)) {

						continue;

					}

				}

				archive.sendTo(auth.user,1,auth,status);

            }

        } else {

            ResponseList<Status> timeline = api.getHomeTimeline(new Paging().count(1));

            if (!timeline.isEmpty()) {

                offset = timeline.get(0).getId();

            } else {

                offset = 0;

            }

        }

		if (auth.tl_offset == null || !auth.tl_offset.equals(offset)) {

			auth.tl_offset = offset;

			TAuth.data.setById(auth.id,auth);

		}

    }


}
