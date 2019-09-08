package io.kurumi.ntt.fragment.twitter.list;

import java.util.*;

import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.TApi;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.archive.UserArchive;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.Html;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import cn.hutool.log.StaticLog;

public class TopList extends Fragment {

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerAdminFunction("top");

	}

	class Score implements Comparable<Score> {

		long id;

		int val = 1;

		public Score(long id) {

			this.id = id;

		}

		@Override
		public int compareTo(Score s) {

			return s.val - val;

		}

	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		requestTwitter(user,msg);

	}

	@Override
	public void onTwitterFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {

		HashMap<Long,Score> mutes = new HashMap<>();
		HashMap<Long,Score> blocks = new HashMap<>();

		List<TAuth> all = TAuth.data.getAll();

		for (int index = 0;index < all.size();index ++) {

			StaticLog.debug("TL : {} / {}",index + 1,all.size());

			TAuth auth = all.get(index);

			try {

				for (long block : TApi.getAllBlockIDs(auth.createApi())) {

					Score score = blocks.get(block);

					if (score == null)  blocks.put(block,new Score(block)); else score.val ++;

				}

			} catch (TwitterException e) {}

			try {

				for (long mute : TApi.getAllMuteIDs(auth.createApi())) {

					Score score = mutes.get(mute);

					if (score == null) mutes.put(mute,new Score(mute)); else score.val ++;

				}

			} catch (TwitterException e) {}

		}

		Twitter api = account.createApi();

		TreeSet<Score> mR = new TreeSet<Score>(mutes.values());
		TreeSet<Score> bR = new TreeSet<Score>(blocks.values());

		if (!mR.isEmpty()) {

			String message = "被静音最多的 :\n";

			Iterator<Score> iter = mR.iterator();

			for (int index = 0;index < 10 && iter.hasNext();index ++) {

				Score target = iter.next();
				
				message += "\n" + Html.b(UserArchive.show(api,target.id).name) + " : " + target.val + " 次";

			}

			msg.send(message).html().async();

		}

		if (!bR.isEmpty()) {

			String message = "被屏蔽最多的 :\n";

			Iterator<Score> iter = bR.iterator();

			for (int index = 0;index < 20 && iter.hasNext();index ++) {

				Score target = iter.next();

				message += "\n" + Html.b(UserArchive.show(api,target.id).name) + " : " + target.val + " 次";

			}

			msg.send(message).html().async();


		}



	}



}
