package io.kurumi.ntt.fragment.twitter.status;

import cn.hutool.core.util.ArrayUtil;
import io.kurumi.ntt.db.Data;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.fragment.abs.TwitterFunction;
import io.kurumi.ntt.fragment.abs.request.Send;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.archive.StatusArchive;
import io.kurumi.ntt.utils.NTT;
import java.util.Date;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import twitter4j.Status;
import twitter4j.TwitterException;

public class TimedStatus extends TwitterFunction {

	public static Timer timer;
	
	public static Data<TimedUpdate> data = new Data<TimedUpdate>(TimedUpdate.class);
	
	public static class TimedUpdate {
		
		public long id;
		
		public long time;
		
		public long user;
		
		public long auth;
		
		public String text;

        public LinkedList<Long> images;

        public Long video;
		
        public Long toReply;

		public String attach;
		
	}
	
	public static void start() {
		
		stop();
		
		timer = new Timer("Timed Status Thread");
		
		
		LinkedList<TimedUpdate> updates = new LinkedList<>();
		
		for (TimedUpdate update : data.collection.find()) {
			
			updates.add(update);
			
		}
		
		for (TimedUpdate update : updates) {
			
			schedule(update);
			
		}
		
	}
	
	public static void stop() {
		
		if (timer == null) return;
		
		timer.cancel();
		
		timer = null;
		
	}
	
	public static void schedule(TimedUpdate update) {
		
		timer.schedule(new TimedTask(update),new Date(update.time));
		
	}
	
	public static class TimedTask extends TimerTask {
		
		TimedUpdate update;

		public TimedTask(TimedUpdate update) {
			this.update = update;
		}

		@Override
		public void run() {

			TAuth auth = TAuth.getById(update.auth);
			
			if (auth == null) {
				
				data.deleteById(update.id);
				
				return;
				
			}
			
			twitter4j.StatusUpdate send = new twitter4j.StatusUpdate(update.text == null ? "" : update.text);

            if (update.toReply != null) send.inReplyToStatusId(update.toReply);

            if (update.attach != null) send.attachmentUrl(update.attach);

            if (!update.images.isEmpty()) {

                send.setMediaIds(ArrayUtil.unWrap(update.images.toArray(new Long[update.images.size()])));

            } else if (update.video != -1) {

                //update.auth.createApi().uploadMedia();

                send.setMediaIds(update.video);

            }

            try {

                Status status = auth.createApi().updateStatus(send);

                StatusArchive archive = StatusArchive.save(status);

				data.deleteById(update.id);
				
                new Send(auth.user,"定时推文 " + update.id + " 发送成功 : ",StatusArchive.split_tiny,archive.toHtml(0)).buttons(StatusAction.createMarkup(archive.id,true,archive.depth() == 0,false,false)).html().point(1,archive.id);

            } catch (TwitterException e) {

                new Send(auth.user,"定时推文 " + update.id + " 发送失败 : ",NTT.parseTwitterException(e)).exec();
				
            }

            return;

			
			
		}
		
	}
	
	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {
		
		if (params.length == 0) {
			
			msg.send("/timed <list/submit/remove>").exec();
			
			return;
			
		}
		
		if ("list".equals(params[0])) {
			
			
			
		}
		
	}

	@Override
	public void functions(LinkedList<String> names) {
		
		names.add("timed");
		
	}

}
