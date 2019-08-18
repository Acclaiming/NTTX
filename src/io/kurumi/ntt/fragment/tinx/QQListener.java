package io.kurumi.ntt.fragment.tinx;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpUtil;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendAnimation;
import com.pengrad.telegrambot.request.SendPhoto;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.Launcher;
import io.kurumi.ntt.cqhttp.TinxListener;
import io.kurumi.ntt.cqhttp.Variants;
import io.kurumi.ntt.cqhttp.model.GroupMember;
import io.kurumi.ntt.cqhttp.model.StrangerInfo;
import io.kurumi.ntt.cqhttp.update.GroupDecreaseNotice;
import io.kurumi.ntt.cqhttp.update.GroupIncreaseNotice;
import io.kurumi.ntt.cqhttp.update.GroupRequest;
import io.kurumi.ntt.cqhttp.update.MessageUpdate;
import io.kurumi.ntt.model.request.ButtonMarkup;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.Html;
import java.io.File;

public class QQListener extends TinxListener {

	@Override
	public void onGroupInviteRequest(GroupRequest request) {

		if (ArrayUtil.contains(Env.QQ_ADMINS,request.user_id)) {

			api.setGroupAddRequest(request.flag,Variants.GR_INVITE,true,null);

		} else {

			api.setGroupAddRequest(request.flag,Variants.GR_INVITE,false,null);

		}

	}

	@Override
	public void onPrivate(MessageUpdate msg) {

		if ("/ping".equals(msg.message)) {

			api.sendPrivateMsg(msg.user_id,"pong",false);

		}

	}

	@Override
	public void onGroupKickMember(GroupDecreaseNotice member) {

		Long chatId = TelegramBridge.qqIndex.get(member.group_id);

		if (TelegramBridge.disable.containsKey(chatId)) return;

		String message = StrangerInfo.get(member.user_id,false).nickname + " ( " + member.user_id + " )  被管理员移出了群组.";

		new Send(chatId,message).html().exec();

	}

	@Override
	public void onGroupLeftMember(GroupDecreaseNotice member) {

		Long chatId = TelegramBridge.qqIndex.get(member.group_id);

		if (TelegramBridge.disable.containsKey(chatId)) return;

		String message = StrangerInfo.get(member.user_id,false).nickname  + " ( " + member.user_id + " ) 退出了群组.";

		new Send(chatId,message).html().exec();

	}

	@Override
	public void onGroupIncrease(GroupIncreaseNotice member) {

		Long chatId = TelegramBridge.qqIndex.get(member.group_id);

		if (TelegramBridge.disable.containsKey(chatId)) return;

		String message = GroupMember.get(member.group_id,member.user_id,false).name() + " ( " + member.user_id + " ) 加入了群组.";

		new Send(chatId,message).html().exec();

	}

	@Override
	public void onGroupAddRequest(GroupRequest request) {

		Long chatId = TelegramBridge.qqIndex.get(request.group_id);

		if (TelegramBridge.disable.containsKey(chatId)) return;

		String message = Html.b(StrangerInfo.get(request.user_id,false).nickname) + " ( " + request.user_id + " ) 申请加群.";

		if (StrUtil.isBlank(request.comment)) {

			message += "\n\n验证消息 : " + Html.code(request.comment);

		}

		ButtonMarkup buttons = new ButtonMarkup();

		buttons.newButtonLine("同意",TelegramListener.POINT_ACCEPT,request.flag);

		buttons.newButtonLine()
			.newButton("拒绝",TelegramListener.POINT_REJECT,request.user_id)
			.newButton("拒绝并禁止",TelegramListener.POINT_BLOCK);

		buttons.newButtonLine("忽略",TelegramListener.POINT_IGNORE);

		new Send(chatId,message).buttons(buttons).html().exec();

	}

	@Override
	public void onGroup(MessageUpdate msg) {

		if (!TelegramBridge.qqIndex.containsKey(msg.group_id)) return;

		Long chatId = TelegramBridge.qqIndex.get(msg.group_id);

		if (TelegramBridge.disable.containsKey(chatId)) return;

		String user = Html.b(StrUtil.isBlank(msg.sender.card) ? msg.sender.nickname : msg.sender.card) + " : ";

		String cqImage = "[CQ:image,";

		if (msg.message.contains(cqImage)) {

			String left = StrUtil.subBefore(msg.message,cqImage,false);

			String file = StrUtil.subBetween(msg.message,"file=",",");
			String url = StrUtil.subBetween(msg.message,"url=","]");

			File imageCache = new File(Env.CACHE_DIR,"qq_image/" + file);

			if (!imageCache.isFile()) {

				HttpUtil.downloadFile(url,imageCache);

			}

			if (imageCache.getName().endsWith(".gif")) {

				Launcher.INSTANCE.execute(new SendAnimation(chatId,imageCache).caption(user + left + " [GIF]").parseMode(ParseMode.HTML));

			} else {

				Launcher.INSTANCE.execute(new SendPhoto(chatId,imageCache).caption(user + left + " [图片]").parseMode(ParseMode.HTML));

			}

		} else {

			String message = user;

			msg.message = CqCodeUtil.replaceFace(msg.message);

			message += " " + HtmlUtil.escape(msg.message);

			new Send(chatId,message).html().exec();

		}

	}

}
