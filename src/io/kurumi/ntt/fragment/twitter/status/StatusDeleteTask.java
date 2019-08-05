package io.kurumi.ntt.fragment.twitter.status;

import io.kurumi.ntt.fragment.twitter.TAuth;
import java.util.Timer;
import java.util.TimerTask;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import cn.hutool.core.date.DateUtil;
import java.util.Date;
import io.kurumi.ntt.utils.BotLog;

public class StatusDeleteTask extends TimerTask {

	public static Timer timer = new Timer();

	public static void start() {
		
		timer.schedule(new StatusDeleteTask(),30 * 60 * 100);
		
	}
	
	public static void stop() {
		
		timer.cancel();
		
	}
	

	@Override
	public void run() {

		for (TAuth account : TAuth.data.getAll()) {

			if (account.ad_s == null && account.ad_r == null && account.ad_rt == null) continue;

			try {
				
				executeDelete(account);
				
			} catch (TwitterException e) {
				
				BotLog.error("DELETE STATUS",e);
				
			}

		}

	}

	void executeDelete(TAuth account) throws TwitterException {

		Twitter api = account.createApi();

		ResponseList<Status> timeline = api.getUserTimeline(new Paging().count(200));

		while (timeline != null && !timeline.isEmpty()) {

			for (Status s : timeline) {

				if (account.ad_a != null) {

					// 绝对时间

					if (account.ad_d == null) {

						if (DateUtil.betweenMs(s.getCreatedAt(),new Date()) < 24 * 60 * 60 * 1000) return;

					} else if (account.ad_d == 0) {

						if (DateUtil.betweenMs(s.getCreatedAt(),new Date()) < 3 * 24 * 60 * 60 * 1000) return;

					} else if (account.ad_d == 1) {

						if (DateUtil.betweenMs(s.getCreatedAt(),new Date()) < 7 * 24 * 60 * 60 * 1000) return;

					} else if (account.ad_d == 2) {

						if (DateUtil.betweenMs(s.getCreatedAt(),new Date()) < 30 * 24 * 60 * 60 * 1000) return;

					} else if (account.ad_d == 3) {

						if (DateUtil.betweenMs(s.getCreatedAt(),new Date()) < 2 * 30 * 24 * 60 * 60 * 1000) return;

					} else if (account.ad_d == 4) {

						if (DateUtil.betweenMs(s.getCreatedAt(),new Date()) < 3 * 30 * 24 * 60 * 60 * 1000) return;

					}

				} else {
					
					if (account.ad_d == null) {

						if (DateUtil.betweenDay(s.getCreatedAt(),new Date(),true) < 1) return;

					} else if (account.ad_d == 0) {

						if (DateUtil.betweenDay(s.getCreatedAt(),new Date(),true) < 3) return;
						
					} else if (account.ad_d == 1) {

						if (DateUtil.betweenDay(s.getCreatedAt(),new Date(),true) < 7) return;
						
					} else if (account.ad_d == 2) {

						if (DateUtil.betweenMonth(s.getCreatedAt(),new Date(),true) < 1) return;
						
					} else if (account.ad_d == 3) {

						if (DateUtil.betweenMonth(s.getCreatedAt(),new Date(),true) < 2) return;
						
					} else if (account.ad_d == 4) {

						if (DateUtil.betweenMonth(s.getCreatedAt(),new Date(),true) < 3) return;
						
					}
					
					
				}

				try {

					api.destroyStatus(s.getId());

				} catch (TwitterException e) {

					if (e.getErrorCode() != 144) throw e;

				}

			}

			timeline = api.getUserTimeline(new Paging().count(200));

		}


	}

}
