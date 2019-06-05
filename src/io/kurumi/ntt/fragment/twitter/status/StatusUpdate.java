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

public class StatusUpdate extends TwitterFunction {

	final String POINT_UPDATE_STATUS = "status,update";

	@Override
	public void points(LinkedList<String> points) {

		super.points(points);

		points.add(POINT_UPDATE_STATUS);

	}

	class UpdatePoint {

		String text;

		LinkedList<File> image = new LinkedList<>();

		File video;

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

			msg.send("你没有认证账号，无法直接回复 ~").exec();

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

				msg.reply("回复成功 :",StatusArchive.split_tiny,archive.toHtml(1)).buttons(StatusAction.createMarkup(archive.id,true,archive.depth() <= 1,false,-1,false)).html().point(1,archive.id);

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

			update.image.add(getFile(message.sticker().fileId()));

			msg.send("图片添加成功 已设置 1 / 4 张图片 使用 /submit 发送").exec();

		} else if (message.photo() != null) {

			for (PhotoSize photo : message.photo()) {

				msg.sendUpdatingFile();

				if (photo.fileSize() > 1024 * 1024 * 20) {

					msg.send("图片超过 20m ，根据Telegram官方限制,无法下载").exec();

					return true;

				}

				update.image.add(getFile(photo.fileId()));

				msg.send("图片添加成功 已设置 " + update.image.size() + " / 4 张图片 使用 /submit 发送").exec();

			}

		} else if (message.animation() != null) {

			if (message.animation().fileSize() > 1024 * 1024 * 20) {

				msg.send("视频超过 20m ，根据Telegram官方限制,无法下载").exec();

				return true;

			}

			msg.sendUpdatingFile();

			update.video = getFile(message.animation().fileId());

			msg.send("视频添加成功 使用 /submit 发送").exec();


		} else if (message.video() != null) {

			if (message.video().fileSize() > 1024 * 1024 * 20) {

				msg.send("视频超过 20m ，根据Telegram官方限制,无法下载").exec();

				return true;

			}

			msg.sendUpdatingFile();

			update.video = getFile(message.video().fileId());

			msg.send("视频添加成功 使用 /submit 发送").exec();


		} else if (message.videoNote() != null) {

			if (message.videoNote().fileSize() > 1024 * 1024 * 20) {

				msg.send("视频超过 20m ，根据Telegram官方限制,无法下载").exec();

				return true;

			}

			msg.sendUpdatingFile();

			update.video = getFile(message.videoNote().fileId());

			msg.send("视频添加成功 使用 /submit 发送").exec();


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

			if (update.text == null && update.image.isEmpty() && update.video == null) {

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

			if (!update.image.isEmpty()) {

				for (File image : update.image) {

					send.media(image);

				}

				msg.sendUpdatingPhoto();

			} else if (update.video != null) {

				send.media(update.video);

				msg.sendUpdatingVideo();

			}

			try {


				Status status = update.auth.createApi().updateStatus(send);

				StatusArchive archive = StatusArchive.save(status);

				msg.reply("发送成功 :",StatusArchive.split_tiny,archive.toHtml(1)).buttons(StatusAction.createMarkup(archive.id,true,archive.depth() <= 1,false,-1,false)).html().point(1,archive.id);

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

			if (update.image.size() == 4) {

				msg.send("已经到了四张图片上限 ~").exec();

				return;

			} else if (update.video != null) {

				msg.send("已经有包含视频了 ~").exec();

				return;

			}

			msg.sendUpdatingFile();

			update.image.add(getFile(message.sticker().fileId()));

			msg.send("图片添加成功 已设置 " + update.image.size() + " / 4 张图片 使用 /submit 发送").exec();

		}

		if (message.photo() != null) {

			for (PhotoSize photo : message.photo()) {

				if (update.image.size() == 4) {

					msg.send("已经到了四张图片上限 ~").exec();

					return;

				} else if (update.video != null) {

					msg.send("已经有包含视频了 ~").exec();

					return;

				}

				msg.sendUpdatingFile();

				if (photo.fileSize() > 1024 * 1024 * 20) {

					msg.send("图片超过 20m ，根据Telegram官方限制,无法下载").exec();

					return;

				}

				update.image.add(getFile(photo.fileId()));

				msg.send("图片添加成功 已设置 " + update.image.size() + " / 4 张图片 使用 /submit 发送").exec();

			}

		} else if (message.animation() != null) {

			if (!update.image.isEmpty()) {

				msg.send("已经有添加图片了 无法添加视频 ~").exec();

				return;

			} else if (update.video != null) {

				msg.send("已经有设置视频了 ~").exec();

				return;

			}

			if (message.animation().fileSize() > 1024 * 1024 * 20) {

				msg.send("视频超过 20m ，根据Telegram官方限制,无法下载").exec();

				return;

			}


			msg.sendUpdatingFile();

			update.video = getFile(message.animation().fileId());

			msg.send("视频添加成功 使用 /submit 发送").exec();


		} else if (message.video() != null) {


			if (!update.image.isEmpty()) {

				msg.send("已经有添加图片了 无法添加视频 ~").exec();

				return;

			} else if (update.video != null) {

				msg.send("已经有设置视频了 ~").exec();

				return;

			}

			if (message.video().fileSize() > 1024 * 1024 * 20) {

				msg.send("视频超过 20m ，根据Telegram官方限制,无法下载").exec();

				return;

			}

			msg.sendUpdatingFile();

			update.video = getFile(message.video().fileId());

			msg.send("视频添加成功 使用 /submit 发送").exec();


		} else if (message.videoNote() != null) {

			if (!update.image.isEmpty()) {

				msg.send("已经有添加图片了 无法添加视频 ~").exec();

				return;

			} else if (update.video != null) {

				msg.send("已经有设置视频了 ~").exec();

				return;

			}

			if (message.videoNote().fileSize() > 1024 * 1024 * 20) {

				msg.send("视频超过 20m ，根据Telegram官方限制,无法下载").exec();

				return;

			}

			msg.sendUpdatingFile();

			update.video = getFile(message.videoNote().fileId());

			msg.send("视频添加成功 使用 /submit 发送").exec();


		}

	}

}
