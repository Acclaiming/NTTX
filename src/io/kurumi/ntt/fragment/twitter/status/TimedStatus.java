package io.kurumi.ntt.fragment.twitter.status;

import cn.hutool.core.util.ArrayUtil;
import io.kurumi.ntt.db.Data;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.fragment.abs.TwitterFunction;
import io.kurumi.ntt.fragment.abs.request.Send;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.archive.StatusArchive;
import io.kurumi.ntt.utils.Html;
import io.kurumi.ntt.utils.NTT;
import java.util.Date;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import twitter4j.Status;
import twitter4j.TwitterException;
import cn.hutool.core.util.NumberUtil;
import io.kurumi.ntt.fragment.twitter.status.TimedStatus.TimedUpdate;

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

			if (!data.containsId(update.id)) {

				return; // canceled

			}

			if (auth == null) {

				data.deleteById(update.id);

				return;

			}

			data.deleteById(update.id);

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

                new Send(auth.user,"定时推文 " + update.id + " 发送成功 : ",StatusArchive.split_tiny,archive.toHtml(0)).buttons(StatusAction.createMarkup(archive.id,true,archive.depth() == 0,false,false)).html().point(1,archive.id);

            } catch (TwitterException e) {

                new Send(auth.user,"定时推文 " + update.id + " 发送失败 : ",NTT.parseTwitterException(e)).exec();

            }

            return;



		}

	}

	@Override
	public boolean onPrivate(UserData user,Msg msg) {

		if (super.onPrivate(user,msg)) return true;

		if (!msg.isStartPayload()) return false;

		String[] payload = msg.payload();

		if (payload.length  != 2 || !NumberUtil.isNumber(payload[1])) {

			return false;

		}

		if ("tdc".equals(payload[0])) {

			Long updateId = NumberUtil.parseLong(msg.payload()[1]);

			TimedUpdate update = data.getById(updateId);

			TAuth auth = TAuth.getById(update.auth);

			if (!auth.user.equals(user.id)) {

				msg.send("很抱歉，不能执行这项操作").exec();

				return true;

			}

			data.deleteById(update.id);

			msg.send("定时推文 " + updateId + " 已取消 (").exec();

			return true;

		} else if ("tds".equals(payload[0])) {

			Long updateId = NumberUtil.parseLong(msg.payload()[1]);

			TimedUpdate update = data.getById(updateId);

			TAuth auth = TAuth.getById(update.auth);

			if (!auth.user.equals(user.id)) {

				msg.send("很抱歉，不能执行这项操作").exec();

				return true;

			}

			data.deleteById(update.id);

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

                msg.reply("发送成功 : ",StatusArchive.split_tiny,archive.toHtml(0)).buttons(StatusAction.createMarkup(archive.id,true,archive.depth() == 0,false,false)).html().point(1,archive.id);

            } catch (TwitterException e) {

				msg.reply("发送失败 : ",NTT.parseTwitterException(e)).exec();

            }

			return true;

		} else return false;

	}



	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {

		if (data.countByField("auth",account.id) == 0) {

			msg.send("没有待发送的定时推文，使用 /update 创建新推文").exec();

			return;

		}

		StringBuilder updates = new StringBuilder("定时推文列表 :");

		for (TimedUpdate update : data.findByField("auth",account.id)) {

			updates.append("\n").append(update.id).append(" : ");

			if (update.text != null) {

				updates.append(update.text.length() > 5 ? (update.text.substring(0,5) + "...") : update.text);

			}

			if (!update.images.isEmpty()) {

				for (int index = 0;index < update.images.size();index ++) updates.append(" [图片]");

			}

			if (update.video != -1) updates.append(" [视频]");

			updates.append(" [ ");

			updates.append(Html.startPayload("取消","tdc",update.id));

			updates.append(" ");

			updates.append(Html.startPayload("立即发送","tds",update.id));

			updates.append(" ]");

		}

		msg.send(updates.toString()).html().exec();

	}

	@Override
	public void functions(LinkedList<String> names) {

		names.add("timed");

	}

}
