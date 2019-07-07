package io.kurumi.ntt.fragment.twitter.status;

import cn.hutool.core.date.DateException;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NumberUtil;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.PhotoSize;
import io.kurumi.ntt.db.PointData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.archive.StatusArchive;
import io.kurumi.ntt.fragment.twitter.archive.UserArchive;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.MongoIDs;
import io.kurumi.ntt.utils.NTT;
import java.util.LinkedList;
import twitter4j.Status;
import twitter4j.TwitterException;
import com.pengrad.telegrambot.response.SendResponse;
import io.kurumi.ntt.utils.BotLog;

public class StatusUpdate extends Fragment {

    final String POINT_UPDATE_STATUS = "status_update";

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerFunction("update");

        registerPoint(POINT_UPDATE_STATUS);

	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		requestTwitter(user,msg);

	}

    @Override
    public void onTwitterFunction(UserData user,Msg msg,String function,String[] params,final TAuth account) {

        StatusUpdate.UpdatePoint update = new UpdatePoint();

		update.context.add(msg);
		
        update.auth = account;

        if (msg.isReply()) {

            MessagePoint point = MessagePoint.get(msg.replyTo().messageId());

            if (point != null && point.type == 1) {

                update.quoted = point.targetId;

            }

        }

        setPrivatePoint(user,POINT_UPDATE_STATUS,update);

        msg.send("现在发送推文内容 : ").withCancel().exec(update);

    }

	String submitAndCancel = "使用 /submit 发送\n使用 /timed 定时发送\n使用 /cancel 取消";

	@Override
	public int checkMsg(UserData user,Msg msg) {

		if (!msg.isPrivate() || !msg.isReply()) {

			return PROCESS_ASYNC;

		}

        MessagePoint point = MessagePoint.get(msg.replyTo().messageId());

        if (point == null || point.type == 0) return PROCESS_ASYNC;

        long count = TAuth.data.countByField("user",user.id);

        TAuth auth;

        if (count == 0) {

            msg.send("你没有认证账号，使用 /login 登录 ~").exec();

            return PROCESS_REJECT;

        } else if (count == 1) {

            auth = TAuth.getByUser(user.id).first();

        } else {

            StatusAction.CurrentAccount current = StatusAction.current.getById(user.id);

            if (current != null) {

                auth = TAuth.getById(current.accountId);

                if (auth == null || !auth.user.equals(user.id)) {

                    msg.send("乃认证了多个账号 请使用 /current 选择默认账号再操作 ~").exec();

                    return PROCESS_REJECT;

                }

            } else {

                msg.send("乃认证了多个账号 请使用 /current 选择默认账号再操作 ~").exec();

                return PROCESS_REJECT;

            }

        }

        Message message = msg.message();

        UpdatePoint update = new UpdatePoint();

		update.auth = auth;

        update.toReply = StatusArchive.get(point.targetId);
		
		update.context.add(msg);
		
		if (msg.hasText()) {

            if (msg.text().toCharArray().length > 280) {

                msg.send("大小超过 Twitter 280 字符限制 , 注意 : 一个中文字占两个字符。").exec(update);

                return PROCESS_REJECT;

            }

            update.text = msg.text();

            msg.send("文本已设定",submitAndCancel).exec(update);

        } else if (message.sticker() != null) {

            msg.sendUpdatingFile();

            try {

                update.images.add(NTT.telegramToTwitter(auth.createApi(),message.sticker().fileId(),"sticker.png",0));

                msg.send("图片添加成功 已设置 1 / 4 张图片",submitAndCancel).exec(update);

            } catch (TwitterException e) {

                msg.send("图片上传失败",NTT.parseTwitterException(e)).exec(update);

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

                msg.send("图片超过 5m ，根据Twitter官方限制,无法发送").exec(update);

                return PROCESS_REJECT;

            }

            try {

                update.images.add(NTT.telegramToTwitter(auth.createApi(),max.fileId(),"image.png",0));

                msg.send("图片添加成功 已设置 " + update.images.size() + " / 4 张图片 ",submitAndCancel).exec(update);

            } catch (TwitterException e) {

                msg.send("图片上传失败",NTT.parseTwitterException(e)).exec(update);

            }

        } else if (message.animation() != null) {

            if (message.animation().fileSize() > 1024 * 1024 * 15) {

                msg.send("动图超过 15m ，根据Twitter官方限制,无法发送").exec(update);

                return PROCESS_REJECT;

            }

            msg.sendUpdatingFile();

            try {

                msg.send("正在转码... 这可能需要几分钟的时间...").exec(update);

				msg.sendTyping();

                update.video = NTT.telegramToTwitter(update.auth.createApi(),message.animation().fileId(),message.animation().fileName(),2);

				msg.sendUpdatingVideo();
				
                msg.send("动图添加成功",submitAndCancel).exec(update);

            } catch (TwitterException e) {

                msg.send("动图上传失败",NTT.parseTwitterException(e)).exec(update);

            }


        } else if (message.video() != null) {

            if (message.video().fileSize() > 1024 * 1024 * 15) {

                msg.send("视频超过 15m ，根据Twitter官方限制,无法发送").exec(update);

                return PROCESS_REJECT;

            }

            msg.sendUpdatingFile();

            try {

                update.video = NTT.telegramToTwitter(auth.createApi(),message.video().fileId(),"video.mp4",1);

                msg.send("视频添加成功",submitAndCancel).exec(update);

            } catch (TwitterException e) {

                msg.send("视频上传失败",NTT.parseTwitterException(e)).exec(update);

            }


        } else if (message.videoNote() != null) {

            if (message.videoNote().fileSize() > 1024 * 1024 * 15) {

                msg.send("视频超过 15m ，根据Twitter官方限制,无法发送").exec(update);

                return PROCESS_REJECT;

            }

            msg.sendUpdatingFile();

            try {

                update.video = NTT.telegramToTwitter(auth.createApi(),message.videoNote().fileId(),"video.mp4",1);

                msg.send("视频添加成功",submitAndCancel).exec(update);

            } catch (TwitterException e) {

                msg.send("视频上传失败",NTT.parseTwitterException(e)).exec(update);

            }


        } else {

            return PROCESS_ASYNC;

        }

        setPrivatePoint(user,POINT_UPDATE_STATUS,update);

        return PROCESS_REJECT;

    }

	@Override
	public void onPointedFunction(UserData user,Msg msg,String function,String[] params,String point,PointData data) {

		UpdatePoint update = (UpdatePoint) data;
		
		data.context.add(msg);

		if ("timed".equals(function)) {

			if (update.text == null && update.images.isEmpty() && update.video == -1) {

                msg.send("好像什么内容都没有。？ 请输入文本 / 贴纸 / 图片 / 视频").exec(update);

                return;

			}

			long time = -1;

			if (params.length == 0 || (params.length > 0 && !params[0].contains(":"))) {

				msg.send("/timed 小时:分钟 [年-月-日 可选] [时区 (默认为 +8) 可选]").exec(update);

				return;

			} else { 

				if (params.length == 1) {

					try {

						DateTime date = DateUtil.parse(params[0],"HH:mm");

						DateTime current = new DateTime();

						current.setHours(date.hour(true));

						current.setMinutes(date.minute());

						time = current.getTime();

					} catch (DateException ex) {

						msg.send("无效的时间 例子 : /timed 23:59").exec(update);

						return;

					}


				} else if (params.length > 1) {

					try {

						DateTime date = DateUtil.parse(params[1] + " " + params[0],"yyyy-MM-dd HH:mm");

						time = date.getTime();

					} catch (DateException ex) {

						msg.send("无效的时间 例子 : /timed 12:59 2019-12-31").exec(update);

						return;

					}

					if (params.length > 2) {

						String offset = params[2];

						if (!NumberUtil.isNumber(offset)) {

							msg.send("无效的时区 例子 : /timed 12:59 2019-12-31 8").exec(update);

							return;

						}

						time = time + (60 * 60 * 1000 * (NumberUtil.parseInt(offset) - 8));

					}

				}

			}

			if (time < (System.currentTimeMillis() + (10 * 1000))) {

				msg.send("这个时间已经过去了...").exec(update);

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

			clearPrivatePoint(user);

			return;

		} else if ("submit".equals(function)) {

            if (update.text == null && update.images.isEmpty() && update.video == -1) {

                msg.send("好像什么内容都没有。？ 请输入文本 / 贴纸 / 图片 / 视频").exec(update);

                return;

            }

            clearPrivatePoint(user);

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

                msg.send(update.toReply == null ? "发送成功 :" : "回复成功 :",StatusArchive.split_tiny,archive.toHtml(0)).buttons(StatusAction.createMarkup(archive.id,true,archive.depth() == 0,false,false)).html().point(1,archive.id);

            } catch (TwitterException e) {

                msg.send(update.toReply == null ? "发送失败 :(" : "回复失败 :(",NTT.parseTwitterException(e)).exec();

            }

            return;

        }


	}

	@Override
	public void onPoint(UserData user,Msg msg,String point,PointData data) {

		UpdatePoint update = (UpdatePoint) data;

		data.context.add(msg);
		
        if (msg.hasText()) {

            if (msg.text().toCharArray().length > 280) {

                msg.send("大小超过 Twitter 280 字符限制 , 注意 : 一个中文字占两个字符。").exec(update);

                return;

            }

            update.text = msg.text();
			
            msg.send("文本已设定",submitAndCancel).exec(update);

        }

        Message message = msg.message();

        if (message.sticker() != null) {

            if (update.images.size() == 4) {

                msg.send("已经到了四张图片上限 ~").exec(update);

                return;

            } else if (update.video != -1) {

                msg.send("已经有包含视频了 ~").exec(update);

                return;

            }

            msg.sendUpdatingFile();

            try {

                update.images.add(NTT.telegramToTwitter(update.auth.createApi(),message.sticker().fileId(),"sticker.png",0));

                msg.send("图片添加成功 已设置 " + update.images.size() + " / 4 张图片 ",submitAndCancel).exec(update);

            } catch (TwitterException e) {

                msg.send("图片上传失败",NTT.parseTwitterException(e)).exec(update);

            }

        }

        if (message.photo() != null) {

            if (update.images.size() == 4) {

                msg.send("已经到了四张图片上限 ~").exec(update);

                return;

            } else if (update.video != -1) {

                msg.send("已经有添加视频了 ~").exec(update);

                return;

            }

            PhotoSize max = null;

            for (PhotoSize photo : message.photo()) {

                if ((max == null || photo.fileSize() > max.fileSize()) && photo.fileSize() < 1024 * 1024 * 5) {

                    max = photo;

                }

            }

            if (max == null) {

                msg.send("图片超过 5m ，根据Twitter官方限制,无法发送").exec(update);

                return;

            }

            msg.sendUpdatingFile();

            try {

                update.images.add(NTT.telegramToTwitter(update.auth.createApi(),max.fileId(),"image.png",0));

                msg.send("图片添加成功 已设置 " + update.images.size() + " / 4 张图片 ",submitAndCancel).exec(update);

            } catch (TwitterException e) {

                msg.send("图片上传失败",NTT.parseTwitterException(e)).exec(update);

            }


        } else if (message.animation() != null) {

            if (!update.images.isEmpty()) {

                msg.send("已经有添加图片了 无法添加视频 ~").exec(update);

                return;

            } else if (update.video != -1) {

                msg.send("已经有设置视频了 ~").exec(update);

                return;

            }

            if (message.animation().fileSize() > 1024 * 1024 * 15) {

                msg.send("动图超过 15m ，根据Twitter官方限制,无法发送").exec(update);

                return;

            }


            try {
				
				msg.send("正在转码... 这可能需要几分钟的时间...").exec(update);

				msg.sendTyping();
				
                update.video = NTT.telegramToTwitter(update.auth.createApi(),message.animation().fileId(),message.animation().fileName(),2);

				msg.sendUpdatingVideo();
				
                msg.send("动图添加成功",submitAndCancel).exec(update);

            } catch (TwitterException e) {

                msg.send("动图上传失败",NTT.parseTwitterException(e)).exec(update);

            }

        } else if (message.video() != null) {

            if (!update.images.isEmpty()) {

                msg.send("已经有添加图片了 无法添加视频 ~").exec(update);

                return;

            } else if (update.video != -1) {

                msg.send("已经有设置视频了 ~").exec(update);

                return;

            }

            if (message.video().fileSize() > 1024 * 1024 * 15) {

                msg.send("视频超过 15m ，根据Twitter官方限制,无法发送").exec(update);

                return;

            }

            msg.sendUpdatingFile();

            try {

                update.video = NTT.telegramToTwitter(update.auth.createApi(),message.video().fileId(),"video.mp4",1);

                msg.send("视频添加成功",submitAndCancel).exec(update);

            } catch (TwitterException e) {

                msg.send("图片上传失败",NTT.parseTwitterException(e)).exec(update);

            }


        } else if (message.videoNote() != null) {

            if (!update.images.isEmpty()) {

                msg.send("已经有添加图片了 无法添加视频 ~").exec(update);

                return;

            } else if (update.video != -1) {

                msg.send("已经有设置视频了 ~").exec(update);

                return;

            }

            if (message.videoNote().fileSize() > 1024 * 1024 * 15) {

                msg.send("视频超过 15m ，根据Twitter官方限制,无法发送").exec(update);

                return;

            }

            msg.sendUpdatingFile();

            try {

                update.video = NTT.telegramToTwitter(update.auth.createApi(),message.videoNote().fileId(),"video.mp4",1);

                msg.send("视频添加成功",submitAndCancel).exec(update);

            } catch (TwitterException e) {

                msg.send("图片上传失败",NTT.parseTwitterException(e)).exec(update);

            }


        }

    }

    class UpdatePoint extends PointData {

        String text;

        LinkedList<Long> images = new LinkedList<>();

        long video = -1;

        TAuth auth;

        StatusArchive toReply;

        Long quoted;

    }

}
