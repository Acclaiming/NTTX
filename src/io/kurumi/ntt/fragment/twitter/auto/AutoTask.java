package io.kurumi.ntt.fragment.twitter.auto;

import io.kurumi.ntt.model.request.*;
import io.kurumi.ntt.twitter.*;
import java.util.*;
import twitter4j.*;

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

		for (Status status : tl) {

			if (status.isFavorited()) continue;

			int count = 0;
			
			try {

				api.createFavorite(status.getId());

				count ++;
				
			} catch (TwitterException ex) {}
			
			new Send(auth.user,"sended " + count + " to home_timeline").exec();

		}

	}

}
