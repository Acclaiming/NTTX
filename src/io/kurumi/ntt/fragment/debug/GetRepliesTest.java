package io.kurumi.ntt.fragment.debug;

import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.archive.StatusArchive;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.NTT;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import io.kurumi.ntt.utils.TwitterWeb;
import java.util.HashSet;
import cn.hutool.core.util.ArrayUtil;
import twitter4j.ResponseList;
import java.util.TreeSet;

public class GetRepliesTest extends Fragment {

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerFunction("get_replies");

	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		if (params.length != 1) {

            msg.send("用法 /status <推文链接|ID>").publicFailed();

            return;

        }

        if (NTT.parseStatusId(params[0]) == -1L) {

            msg.send("用法 /status <推文链接|ID>").publicFailed();

            return;

        }

		requestTwitter(user,msg);

	}

	@Override
	public void onTwitterFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {

		Twitter api = account.createApi();

        msg.sendTyping();

        Long statusId = NTT.parseStatusId(params[0]);

		String screenName;

        if (StatusArchive.contains(statusId)) {

			screenName = StatusArchive.get(statusId).user().screenName;

        } else {

			try {

				Status newStatus = api.showStatus(statusId);

				StatusArchive archive = StatusArchive.save(newStatus).loop(api);

				archive.sendTo(msg.chatId(),-1,account,msg.isPrivate() ? newStatus : null);

				screenName = archive.user().screenName;

			} catch (TwitterException ex) {

				TAuth auth = NTT.loopFindAccessable(NTT.parseScreenName(params[0]));

				if (auth == null) {

					msg.send(NTT.parseTwitterException(ex)).async();

					return;

				}

                api = auth.createApi();

			}

			try {

				Status newStatus = api.showStatus(statusId);

				StatusArchive archive = StatusArchive.save(newStatus);

				archive.loop(api);

				screenName = archive.user().screenName;

			} catch (TwitterException e) {

				msg.send(NTT.parseTwitterException(e)).async();

				return;


			}
			
		}
		
		TreeSet<Long> repliesSet = TwitterWeb.fetchStatusReplies(screenName,statusId,true);

		try {
			
			ResponseList<Status> replies = api.lookup(ArrayUtil.unWrap(repliesSet.toArray(new Long[repliesSet.size()])));

			for (Status reply : replies) {
				
				StatusArchive.save(reply).loop(api).sendTo(msg.chatId(),1,account,reply);
				
			}
			
		} catch (TwitterException e) {
			
			msg.send(NTT.parseTwitterException(e)).async();
			
		}

	}


}
