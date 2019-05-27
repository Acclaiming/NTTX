package io.kurumi.ntt.fragment.twitter.auto;

import io.kurumi.ntt.model.request.*;
import io.kurumi.ntt.twitter.*;
import java.util.*;
import twitter4j.*;
import cn.hutool.core.util.*;

public class AutoTask extends TimerTask {

	public static AutoTask INSTANCE = new AutoTask();

	public static Timer timer;

	public static void start() {

		stop();

		timer = new Timer();

		timer.scheduleAtFixedRate(INSTANCE,new Date(),15 * 60 * 1000);

	}

	public static void stop() {

		if (timer != null) {

			timer.cancel();

			timer = null;

		}

	}

	@Override
	public void run() {

		for (AutoUI.AutoSetting auto :  AutoUI.autoData.collection.find()) {

			TAuth auth = TAuth.getById(auto.id);

			if (auto.like) {

				try {

					startLikeService(auth);

				} catch (TwitterException e) {

					auto.like = false;

					AutoUI.autoData.setById(auto.id,auto);

					new Send(auth.user,"auto.like disabled : " + e).exec();

				}

			}

		}

	}

	void startLikeService(TAuth auth) throws TwitterException {

		Twitter api = auth.createApi();

		ResponseList<Status> tl = api.getHomeTimeline(new Paging().count(800));

		int count = 0;

		for (Status status : ArrayUtil.reverse(tl.toArray(new Status[tl.size()]))) {

			if (status.isFavorited()) continue;

			if (auth.id.equals(status.getUser().getId())) continue;

			try {

				api.createFavorite(status.getId());

				count ++;

			} catch (TwitterException ex) {}

		}

		if (count > 0) {

			new Send(auth.user,"sended " + count + " likes to home_timeline (" + auth.archive().urlHtml() + ")").html().exec();
			
		}

	}

}
