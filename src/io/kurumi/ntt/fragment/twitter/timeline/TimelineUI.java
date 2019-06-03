package io.kurumi.ntt.fragment.twitter.timeline;

import io.kurumi.ntt.db.*;
import io.kurumi.ntt.funcs.abs.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.model.request.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.twitter.archive.*;
import java.util.*;
import twitter4j.*;
import io.kurumi.ntt.fragment.twitter.status.*;

public class TimelineUI extends TwitterFunction {

	public static Data<TLSetting> data = new Data<TLSetting>(TLSetting.class);

	public class TLSetting {

		public long id;

		public boolean mention = false;
		public boolean timeline = false;

		public long mentionOffset = -1;
		public long timelineOffset = -1;

	}

	@Override
	public void functions(LinkedList<String> names) {

		names.add("timeline");

		names.add("mention");

	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {

		TLSetting setting = data.getById(user.id);

		if (setting == null) {
			
			setting = new TLSetting();
			
			setting.id = account.id;
			
		}
		
		boolean target = !"off".equals(msg.text());

		msg.send((("timeline".equals(function) ? setting.timeline : setting.mention) == target ? (target ? "无须重复开启" : "没有开启") : ("timeline".equals(function) ? (setting.timeline = target) : (setting.mention = target)) ? "已开启" : "已关闭")).exec();

		if (setting.timeline || setting.mention) {

			data.setById(account.id,setting);

		} else {

			data.deleteById(account.id);

		}

	}

	static Timer timer;
	
	public static void start() {
	
		stop();
		
		timer = new Timer("NTT Timeline Task");
		
		timer.schedule(new TLTask(),new Date());
		
	}
	
	public static void stop() {
		
		if (timer != null) {
		
		timer.cancel();
		
		timer = null;
		
		}
		
	}

	public static class TLTask extends TimerTask {
		
		@Override
		public void run() {
		
			LinkedList<Long> toRemove = new LinkedList<>();

			for (TLSetting setting : data.collection.find()) {

				TAuth auth = TAuth.getById(setting.id);

				if (auth == null) {

					toRemove.add(setting.id);

					continue;

				}

				Twitter api = auth.createApi();

				if (setting.mention) {

					try {
						
						processMention(auth,api,setting);
						
					} catch (TwitterException e) {}

				}

			}

			for (long remove : toRemove) {

				data.deleteById(remove);

			}
			
			long users = data.countByField("mention",true);

			long delay = (((users / (100000 / 24 / 60)) + 1) * 60 * 1000);
			
			timer = new Timer("NTT Timeline Task");
			
			timer.schedule(new TLTask(),new Date(System.currentTimeMillis() + delay));
			
		}

		static void processMention(TAuth auth,Twitter api,TLSetting setting) throws TwitterException {

			if (setting.mentionOffset != -1) {

				ResponseList<Status> mentions = api.getMentionsTimeline(new Paging().count(800).sinceId(setting.mentionOffset + 1));

				for (Status mention : mentions) {
					
					StatusArchive archive = StatusArchive.save(mention,api);

					new Send(auth.user,archive.toHtml(1)).buttons(StatusAction.createMarkup(mention,auth.id.equals(mention.getUser().getId()),archive.depth() <= 1)).html().point(1,archive.id);
					
				}
				
			} else {
				
				ResponseList<Status> mention = api.getMentionsTimeline(new Paging().count(1));

				if (mention.isEmpty()) {
					
					setting.mentionOffset = 0;
					
				} else {
					
					setting.mentionOffset = mention.get(0).getId();
					
				}
				
			}
			
			data.setById(auth.id,setting);

		}

	}

}
