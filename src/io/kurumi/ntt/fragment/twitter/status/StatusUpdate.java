package io.kurumi.ntt.fragment.twitter.status;

import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.funcs.abs.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.model.request.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.twitter.archive.*;
import io.kurumi.ntt.utils.*;
import java.io.*;
import java.util.*;
import twitter4j.*;

import java.io.File;
import io.kurumi.ntt.fragment.twitter.status.StatusAction.*;
import cn.hutool.core.util.*;

public class StatusUpdate extends TwitterFunction {

	final String POINT_UPDATE_STATUS = "status,update";

	@Override
	public void points(LinkedList<String> points) {

		super.points(points);

		points.add(POINT_UPDATE_STATUS);

	}

	class UpdatePoint {

		String text;

		LinkedList<Long> images = new LinkedList<>();

		long video = -1;

		TAuth auth;

		StatusArchive toReply;

	}

	@Override
	public void functions(LinkedList<String> names) {

		names.add("update");

	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params,final TAuth account) {

		setPoint(user,POINT_UPDATE_STATUS,new UpdatePoint() {{ auth = account; }});

		msg.send("现在发送推文内容 : [文本/图片/贴纸/视频]").exec();

	}

	@Override
	public boolean onPrivate(UserData user,Msg msg) {

		if (!msg.isReply()) return false;

		MessagePoint point = MessagePoint.get(msg.replyTo().messageId());

		if (point == null) return false;

		if (point.type == 0) return false;

		long count = TAuth.data.countByField("user",user.id);

		TAuth auth;

		if (count == 0) {

			msg.send("你没有认证账号，使用 /login 登录 ~").exec();

			return true;

		} else if (count == 1) {

			auth = TAuth.getByUser(user.id).first();

		} else {

			StatusAction.CurrentAccount current = StatusAction.current.getById(user.id);

			if (current != null) {

				auth = TAuth.getById(current.accountId);

				if (auth == null || !auth.user.equals(user.id)) {

					msg.send("乃认证了多个账号 请使用 /current 选择默认账号再操作 ~").exec();

					return true;

				}

			} else {

				msg.send("乃认证了多个账号 请使用 /current 选择默认账号再操作 ~").exec();

				return true;

			}



		}

		if (msg.hasText()) {

			msg.sendTyping();

			try {

				Status status = TApi.reply(auth.createApi(),StatusArchive.get(point.targetId),msg.text(),null);

				StatusArchive archive = StatusArchive.save(status);

				msg.reply("回复成功 :",StatusArchive.split_tiny,archive.toHtml(0)).buttons(StatusAction.createMarkup(archive.id,true,archive.depth() == 0,false,-1,false)).html().point(1,archive.id);

			} catch (TwitterException e) {

				msg.send("回复失败 :(",NTT.parseTwitterException(e)).exec();

			}

			return true;

		} 

		Message message = msg.message();

		UpdatePoint update = new UpdatePoint();

		update.auth = auth;

		update.toReply = StatusArchive.get(point.targetId);

		if (message.sticker() != null) {

			msg.sendUpdatingFile();

			try {

				update.images.add(NTT.telegramToTwitter(auth.createApi(),message.sticker().fileId(),"sticker.png",true));

				msg.send("图片添加成功 已设置 1 / 4 张图片","使用 /submit 发送","使用 /cancel 取消").exec();

			} catch (TwitterException e) {

				msg.send("图片上传失败",NTT.parseTwitterException(e)).exec();

			}

		} else if (message.photo() != null) {

			PhotoSize max = null;

			for (PhotoSize photo : message.photo()) {

				if ((max == null || photo.fileSize() > max.fileSize()) && photo.fileSize() < 1024 * 1024 * 5) {

					max = photo;

				}

			}

			msg.sendUpdatingFile();

			if (max == null) {

				msg.send("图片超过 5m ，根据Twitter官方限制,无法发送").exec();

				return true;

			}

			try {

				update.images.add(NTT.telegramToTwitter(auth.createApi(),max.fileId(),"image.png",true));

				msg.send("图片添加成功 已设置 " + update.images.size() + " / 4 张图片 ","使用 /submit 发送","使用 /cancel 取消").exec();

			} catch (TwitterException e) {

				msg.send("图片上传失败",NTT.parseTwitterException(e)).exec();

			}

		} else if (message.animation() != null) {

			if (message.animation().fileSize() > 1024 * 1024 * 15) {

				msg.send("动图超过 15m ，根据Twitter官方限制,无法发送").exec();

				return true;

			}

			msg.sendUpdatingFile();

			try {

				update.video = NTT.telegramToTwitter(auth.createApi(),message.animation().fileId(),message.animation().fileName(),false);

				msg.send("动图添加成功","使用 /submit 发送","使用 /cancel 取消").exec();

			} catch (TwitterException e) {

				msg.send("动图上传失败",NTT.parseTwitterException(e)).exec();

			}


		} else if (message.video() != null) {

			if (message.video().fileSize() > 1024 * 1024 * 15) {

				msg.send("视频超过 15m ，根据Twitter官方限制,无法发送").exec();

				return true;

			}

			msg.sendUpdatingFile();

			try {

				update.video = NTT.telegramToTwitter(auth.createApi(),message.video().fileId(),"video.mp4",false);

				msg.send("视频添加成功","使用 /submit 发送","使用 /cancel 取消").exec();

			} catch (TwitterException e) {

				msg.send("视频上传失败",NTT.parseTwitterException(e)).exec();

			}


		} else if (message.videoNote() != null) {

			if (message.videoNote().fileSize() > 1024 * 1024 * 15) {

				msg.send("视频超过 15m ，根据Twitter官方限制,无法发送").exec();

				return true;

			}

			msg.sendUpdatingFile();

			try {

				update.video = NTT.telegramToTwitter(auth.createApi(),message.videoNote().fileId(),"video.mp4",false);

				msg.send("视频添加成功","使用 /submit 发送","使用 /cancel 取消").exec();

			} catch (TwitterException e) {

				msg.send("视频上传失败",NTT.parseTwitterException(e)).exec();

			}



		} else {

			return false;

		}

		setPoint(user,POINT_UPDATE_STATUS,update);

		return true;

	}

	@Override
	public void onPoint(UserData user,Msg msg,PointStore.Point point) {

		UpdatePoint update = (StatusUpdate.UpdatePoint) point.data;

		if ("submit".equals(msg.command())) {

			if (update.text == null && update.images.isEmpty() && update.video == -1) {

				msg.send("好像什么内容都没有。？ 请输入文本 / 贴纸 / 图片 / 视频").exec();

				return;

			}

			clearPoint(user);

			if (update.toReply != null) {

				String reply = "@" + update.toReply.user().screenName + " ";

				if (!update.toReply.userMentions.isEmpty()) {

					for (long mention : update.toReply.userMentions) {

						if (!update.auth.id.equals(mention)) {

							reply = reply + "@" + UserArchive.get(mention).screenName + " ";

						}

					}

				}

				update.text = reply + (update.text == null ? "" : update.text);

			}


			twitter4j.StatusUpdate send = new twitter4j.StatusUpdate(update.text == null ? "" : update.text);

			if (update.toReply != null) send.inReplyToStatusId(update.toReply.id);

			if (!update.images.isEmpty()) {

				send.setMediaIds(ArrayUtil.unWrap(update.images.toArray(new Long[update.images.size()])));

				msg.sendUpdatingPhoto();

			} else if (update.video != -1) {

				//update.auth.createApi().uploadMedia();

				send.setMediaIds(update.video);

				msg.sendUpdatingVideo();

			}

			try {

				Status status = update.auth.createApi().updateStatus(send);

				StatusArchive archive = StatusArchive.save(status);

				msg.reply("发送成功 :",StatusArchive.split_tiny,archive.toHtml(0)).buttons(StatusAction.createMarkup(archive.id,true,archive.depth() == 0,false,-1,false)).html().point(1,archive.id);

			} catch (TwitterException e) {
		
					msg.send("发送失败 :(",NTT.parseTwitterException(e)).exec();

			}

			return;

		}

		if (msg.hasText()) {

			if (msg.text().toCharArray().length > 280) {

				msg.send("大小超过 Twitter 280 字符限制 , 注意 : 一个中文字占两个字符。").exec();

				return;

			}

			update.text = msg.text();

			msg.send("文本已设定 使用 /submit 发送 ~").exec();

		}

		Message message = msg.message();

		if (message.sticker() != null) {

			if (update.images.size() == 4) {

				msg.send("已经到了四张图片上限 ~").exec();

				return;

			} else if (update.video != -1) {

				msg.send("已经有包含视频了 ~").exec();

				return;

			}

			msg.sendUpdatingFile();

			try {

				update.images.add(NTT.telegramToTwitter(update.auth.createApi(),message.sticker().fileId(),"sticker.png",true));

				msg.send("图片添加成功 已设置 " + update.images.size() + " / 4 张图片 ","使用 /submit 发送","使用 /cancel 取消").exec();

			} catch (TwitterException e) {

				msg.send("图片上传失败",NTT.parseTwitterException(e)).exec();

			}

		}

		if (message.photo() != null) {

			if (update.images.size() == 4) {

				msg.send("已经到了四张图片上限 ~").exec();

				return;

			} else if (update.video != -1) {

				msg.send("已经有添加视频了 ~").exec();

				return;

			}

			PhotoSize max = null;

			for (PhotoSize photo : message.photo()) {

				if ((max == null || photo.fileSize() > max.fileSize()) && photo.fileSize() < 1024 * 1024 * 5) {

					max = photo;

				}

			}

			if (max == null) {

				msg.send("图片超过 5m ，根据Twitter官方限制,无法发送").exec();

				return;

			}

			msg.sendUpdatingFile();

			try {

				update.images.add(NTT.telegramToTwitter(update.auth.createApi(),max.fileId(),"image.png",true));

				msg.send("图片添加成功 已设置 " + update.images.size() + " / 4 张图片 ","使用 /submit 发送","使用 /cancel 取消").exec();

			} catch (TwitterException e) {

				msg.send("图片上传失败",NTT.parseTwitterException(e)).exec();

			}



		} else if (message.animation() != null) {

			if (!update.images.isEmpty()) {

				msg.send("已经有添加图片了 无法添加视频 ~").exec();

				return;

			} else if (update.video != -1) {

				msg.send("已经有设置视频了 ~").exec();

				return;

			}

			if (message.animation().fileSize() > 1024 * 1024 * 15) {

				msg.send("动图超过 15m ，根据Twitter官方限制,无法发送").exec();

				return;

			}


			msg.sendUpdatingFile();

			try {

				update.video = NTT.telegramToTwitter(update.auth.createApi(),message.animation().fileId(),message.animation().fileName(),false);

				msg.send("动图添加成功","使用 /submit 发送","使用 /cancel 取消").exec();

			} catch (TwitterException e) {

				msg.send("动图上传失败",NTT.parseTwitterException(e)).exec();

			}

		} else if (message.video() != null) {

			if (!update.images.isEmpty()) {

				msg.send("已经有添加图片了 无法添加视频 ~").exec();

				return;

			} else if (update.video != -1) {

				msg.send("已经有设置视频了 ~").exec();

				return;

			}

			if (message.video().fileSize() > 1024 * 1024 * 15) {

				msg.send("视频超过 15m ，根据Twitter官方限制,无法发送").exec();

				return;

			}

			msg.sendUpdatingFile();

			try {

				update.video = NTT.telegramToTwitter(update.auth.createApi(),message.video().fileId(),"video.mp4",false);

				msg.send("视频添加成功","使用 /submit 发送","使用 /cancel 取消").exec();

			} catch (TwitterException e) {

				msg.send("图片上传失败",NTT.parseTwitterException(e)).exec();

			}


		} else if (message.videoNote() != null) {

			if (!update.images.isEmpty()) {

				msg.send("已经有添加图片了 无法添加视频 ~").exec();

				return;

			} else if (update.video != -1) {

				msg.send("已经有设置视频了 ~").exec();

				return;

			}

			if (message.videoNote().fileSize() > 1024 * 1024 * 15) {

				msg.send("视频超过 15m ，根据Twitter官方限制,无法发送").exec();

				return;

			}

			msg.sendUpdatingFile();

			try {

				update.video = NTT.telegramToTwitter(update.auth.createApi(),message.videoNote().fileId(),"video.mp4",false);

				msg.send("视频添加成功","使用 /submit 发送","使用 /cancel 取消").exec();

			} catch (TwitterException e) {

				msg.send("图片上传失败",NTT.parseTwitterException(e)).exec();

			}


		}

	}

}
