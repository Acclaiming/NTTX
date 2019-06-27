package io.kurumi.ntt.fragment.twitter.status;

import cn.hutool.core.util.ArrayUtil;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.PhotoSize;
import io.kurumi.ntt.db.PointStore;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.fragment.abs.TwitterFunction;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.archive.StatusArchive;
import io.kurumi.ntt.fragment.twitter.archive.UserArchive;
import io.kurumi.ntt.utils.NTT;

import java.util.LinkedList;

import twitter4j.Status;
import twitter4j.TwitterException;
import io.kurumi.ntt.fragment.twitter.status.StatusUpdate.UpdatePoint;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.exceptions.UtilException;
import io.kurumi.ntt.utils.MongoIDs;

public class StatusUpdate extends TwitterFunction {

    final String POINT_UPDATE_STATUS = "status,update";

    @Override
    public void points(LinkedList<String> points) {

        super.points(points);

        points.add(POINT_UPDATE_STATUS);

    }

    @Override
    public boolean useCurrent() {

        return true;

    }

    @Override
    public void functions(LinkedList<String> names) {

        names.add("update");

    }

	@Override
	public boolean async() {

		return false;

	}

    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params,final TAuth account) {

        StatusUpdate.UpdatePoint update = new UpdatePoint();

        update.auth = account;

        if (msg.isReply()) {

            MessagePoint point = MessagePoint.get(msg.replyTo().messageId());

            if (point != null && point.type == 1) {

                update.quoted = point.targetId;

            }

        }

        setPoint(user,POINT_UPDATE_STATUS,update);

        msg.send("现在发送推文内容 : ").withCancel().exec();

    }
	
	String submitAndCancel = "使用 /submit 发送\n使用 /timed 定时发送\n使用 /cancel 取消";

    @Override
    public boolean onPrivate(UserData user,Msg msg) {

		if (super.onPrivate(user,msg)) return true;

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

            StatusArchive toReply = StatusArchive.get(point.targetId);

            String reply = "@" + toReply.user().screenName + " ";

            if (!toReply.userMentions.isEmpty()) {

                for (long mention : toReply.userMentions) {

                    if (!auth.id.equals(mention)) {

                        reply = reply + "@" + UserArchive.get(mention).screenName + " ";

                    }

                }

            }


            String text = text = reply + msg.text();

            twitter4j.StatusUpdate send = new twitter4j.StatusUpdate(text);

            send.inReplyToStatusId(toReply.id);

            try {

                Status status = auth.createApi().updateStatus(send);

                StatusArchive archive = StatusArchive.save(status);

                msg.reply("回复成功 :",StatusArchive.split_tiny,archive.toHtml(0)).buttons(StatusAction.createMarkup(archive.id,true,archive.depth() == 0,false,false)).html().point(1,archive.id);

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

                update.images.add(NTT.telegramToTwitter(auth.createApi(),message.sticker().fileId(),"sticker.png",0));

                msg.send("图片添加成功 已设置 1 / 4 张图片",submitAndCancel).exec();

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

                update.images.add(NTT.telegramToTwitter(auth.createApi(),max.fileId(),"image.png",0));

                msg.send("图片添加成功 已设置 " + update.images.size() + " / 4 张图片 ",submitAndCancel).exec();

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

                update.video = NTT.telegramToTwitter(auth.createApi(),message.animation().fileId(),message.animation().fileName(),2);

                msg.send("动图添加成功",submitAndCancel).exec();

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

                update.video = NTT.telegramToTwitter(auth.createApi(),message.video().fileId(),"video.mp4",1);

                msg.send("视频添加成功",submitAndCancel).exec();

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

                update.video = NTT.telegramToTwitter(auth.createApi(),message.videoNote().fileId(),"video.mp4",1);

                msg.send("视频添加成功",submitAndCancel).exec();

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

		super.onPoint(user,msg,point);

        UpdatePoint update = (StatusUpdate.UpdatePoint) point.data;

		if ("timed".equals(msg.command())) {

			if (update.text == null && update.images.isEmpty() && update.video == -1) {

                msg.send("好像什么内容都没有。？ 请输入文本 / 贴纸 / 图片 / 视频").exec();

                return;

            }

			String[] params = msg.params();

			long time = -1;

			if (params.length == 0 || (params.length > 0 && !params[0].contains(":"))) {

				msg.send("/timed 小时:分钟 [年-月-日 可选] [时区 (默认为 +8) 可选]").exec();

				return;

			} else { 

				if (params.length == 1) {

					try {

						DateTime date = DateUtil.parse(params[0],"HH:mm");

						time = date.getTime();

					} catch (UtilException ex) {

						msg.send("无效的时间").exec();

						return;

					}


				} else if (params.length > 1) {

					try {

						DateTime date = DateUtil.parse(params[1] + " " + params[0],"yyyy-MM-dd HH:mm");

						time = date.getTime();

					} catch (UtilException ex) {

						msg.send("无效的时间").exec();

						return;

					}

					if (params.length > 2) {

						String offset = params[2];

						if (!NumberUtil.isNumber(offset)) {

							msg.send("无效的时区").exec();

							return;

						}

						time = time + (60 * 60 * 1000 * NumberUtil.parseInt(offset));

					}

				}

			}
			
			if (time < (System.currentTimeMillis() + (10 * 1000))) {
				
				msg.send("这个时间已经过去了...").exec();
				
				return;
				
			}
			
			TimedStatus.TimedUpdate timed = new TimedStatus.TimedUpdate();
			
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

            if (update.quoted != null) {

                StatusArchive quoted = StatusArchive.get(update.quoted);

                if (quoted != null) {

                    //update.text = update.text +  " " + quoted.url();

                    timed.attach = quoted.url();

                } else {

                    timed.attach = "https://twitter.com/_/" + update.quoted;

                    //update.text = update.text + " " + attach;

                }

            }

			timed.auth = update.auth.id;
			timed.time = time;
			timed.images = update.images;
			timed.text = update.text;
			timed.video = update.video;
			
			timed.id = MongoIDs.getNextId(TimedStatus.TimedUpdate.class.getSimpleName());

			TimedStatus.data.setById(timed.id,timed);
			
			TimedStatus.schedule(timed);
			
			msg.send("已创建定时推文 : " + timed.id,"使用 /timed 管理定时推文").exec();
			
        } else if ("submit".equals(msg.command())) {

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


            String attach = null;

            if (update.quoted != null) {

                StatusArchive quoted = StatusArchive.get(update.quoted);

                if (quoted != null) {

                    //update.text = update.text +  " " + quoted.url();

                    attach = quoted.url();

                } else {

                    attach = "https://twitter.com/user/" + update.quoted;

                    //update.text = update.text + " " + attach;

                }

            }


            twitter4j.StatusUpdate send = new twitter4j.StatusUpdate(update.text == null ? "" : update.text);

            if (update.toReply != null) send.inReplyToStatusId(update.toReply.id);

            if (attach != null) send.attachmentUrl(attach);

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

                msg.reply(update.toReply == null ? "发送成功 :" : "回复成功 :",StatusArchive.split_tiny,archive.toHtml(0)).buttons(StatusAction.createMarkup(archive.id,true,archive.depth() == 0,false,false)).html().point(1,archive.id);

            } catch (TwitterException e) {

                msg.send(update.toReply == null ? "发送失败 :(" : "回复失败 :(",NTT.parseTwitterException(e)).exec();

            }

            return;

        }

        if (msg.hasText()) {

            if (msg.text().toCharArray().length > 280) {

                msg.send("大小超过 Twitter 280 字符限制 , 注意 : 一个中文字占两个字符。").exec();

                return;

            }

            update.text = msg.text();

            msg.send("文本已设定",submitAndCancel).exec();

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

                update.images.add(NTT.telegramToTwitter(update.auth.createApi(),message.sticker().fileId(),"sticker.png",0));

                msg.send("图片添加成功 已设置 " + update.images.size() + " / 4 张图片 ",submitAndCancel).exec();

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

                update.images.add(NTT.telegramToTwitter(update.auth.createApi(),max.fileId(),"image.png",0));

                msg.send("图片添加成功 已设置 " + update.images.size() + " / 4 张图片 ",submitAndCancel).exec();

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

                update.video = NTT.telegramToTwitter(update.auth.createApi(),message.animation().fileId(),message.animation().fileName(),2);

                msg.send("动图添加成功",submitAndCancel).exec();

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

                update.video = NTT.telegramToTwitter(update.auth.createApi(),message.video().fileId(),"video.mp4",1);

                msg.send("视频添加成功",submitAndCancel).exec();

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

                update.video = NTT.telegramToTwitter(update.auth.createApi(),message.videoNote().fileId(),"video.mp4",1);

                msg.send("视频添加成功",submitAndCancel).exec();

            } catch (TwitterException e) {

                msg.send("图片上传失败",NTT.parseTwitterException(e)).exec();

            }


        }

    }

    class UpdatePoint {

        String text;

        LinkedList<Long> images = new LinkedList<>();

        long video = -1;

        TAuth auth;

        StatusArchive toReply;

        Long quoted;

    }

}
