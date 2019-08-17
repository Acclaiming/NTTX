package io.kurumi.ntt.fragment.qq;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpUtil;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendAnimation;
import com.pengrad.telegrambot.request.SendPhoto;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.Launcher;
import io.kurumi.ntt.cqhttp.TinxListener;
import io.kurumi.ntt.cqhttp.update.MessageUpdate;
import io.kurumi.ntt.db.Data;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.Html;
import java.io.File;
import java.util.HashMap;
import io.kurumi.ntt.cqhttp.update.GroupRequest;
import cn.hutool.core.util.ArrayUtil;
import com.mongodb.client.model.Variable;
import io.kurumi.ntt.cqhttp.Variants;
import io.kurumi.ntt.fragment.qq.TelegramBridge.GroupBind;
import io.kurumi.ntt.utils.NTT;
import io.kurumi.ntt.db.GroupData;

public class TelegramBridge {

	public static Data<GroupBind> data = new Data<>(GroupBind.class);

	public static class GroupBind {

		public Long id;
		public Long groupId;

		public Boolean disable;
		
	}

	public static HashMap<Long,Long> telegramIndex = new HashMap<>();
	public static HashMap<Long,Long> qqIndex = new HashMap<>();
	
	public static HashMap<Long,Boolean> disable = new HashMap<>();
	
	static {

		for (GroupBind bind : data.getAll()) {

			telegramIndex.put(bind.id,bind.groupId);
			qqIndex.put(bind.groupId,bind.id);
			
			if (bind.disable != null) disable.put(bind.id,true);

		}

	}

	public static void qqTotelegram(MessageUpdate update) {


	}

	public static class TelegramListener extends Fragment {
		
		@Override
		public void init(BotFragment origin) {

			super.init(origin);

			registerAdminFunction("tinx_bind","tinx_unbind","tinx_list");

			registerFunction("tinx_enable","tinx_disable");
			
		}

		@Override
		public void onFunction(UserData user,Msg msg,String function,String[] params) {

			if (function.endsWith("_bind")) {

				if (params.length < 2 || !NumberUtil.isNumber(params[0]) || !NumberUtil.isNumber(params[1])) {

					msg.invalidParams("chatId","groupId").async();

					return;

				}

				GroupBind bind = new GroupBind();

				bind.id = NumberUtil.parseLong(params[0]);
				bind.groupId = NumberUtil.parseLong(params[1]);

				telegramIndex.put(bind.id,bind.groupId);
				qqIndex.put(bind.groupId,bind.id);

				data.setById(bind.id,bind);

				msg.send("完成 :)").async();

			} else if (function.endsWith("_unbind")) {
				
				if (params.length < 1 || !NumberUtil.isNumber(params[0])) {
					
					msg.invalidParams("chatId").async();

					return;

				}
				
				data.deleteById(NumberUtil.parseLong(params[0]));
				
				msg.send("完成 :)").async();
				
			} else if (function.endsWith("_list")) {
				
				String message = "所有群组 :\n";
				
				for (GroupBind bind : data.getAll()) {
					
					message += "\n" + Html.code(bind.id) + " ( " + GroupData.get(bind.id).title + " ) -> " + Html.code(bind.groupId);
					
				}
				
				msg.send(message).html().async();
				
			} else if (function.endsWith("_enable")) {
				
				if (!telegramIndex.containsKey(msg.chatId())) {
					
					msg.send("本群没有开启QQ群组消息同步, 请联系机器人管理者.").async();
					
					return;
					
				}
				
				if (NTT.checkGroupAdmin(msg)) return;
				
				if (!disable.containsKey(msg.chatId())) {
					
					msg.send("没有关闭 :)").async();
					
				} else {
					
					disable.remove(msg.chatId());
					
					GroupBind bind = data.getById(msg.chatId());

					bind.disable = null;
					
					data.setById(bind.id,bind);
					
					msg.send("已开启 :) 使用 /tinx_disable 关闭.").async();
					
					return;
					
				}
				
			} else if (function.endsWith("_disable")) {

				if (!telegramIndex.containsKey(msg.chatId())) {

					msg.send("本群没有开启QQ群组消息同步, 请联系机器人管理者.").async();

					return;

				}
				
				if (NTT.checkGroupAdmin(msg)) return;

				if (disable.containsKey(msg.chatId())) {

					msg.send("没有开启 :)").async();

				} else {

					disable.put(msg.chatId(),true);

					GroupBind bind = data.getById(msg.chatId());

					bind.disable = true;

					data.setById(bind.id,bind);

					msg.send("已关闭 :) 使用 /tinx_enable 重新开启.").async();

					return;

				}

			}

		}


		static String formarMessage(UserData user) {

			String message = user.name() + " : ";

			return message;

		}
		
		@Override
		public int checkMsg(UserData user,Msg msg) {

			return msg.isGroup() && !disable.containsKey(msg.chatId()) && telegramIndex.containsKey(msg.chatId()) ? PROCESS_ASYNC : PROCESS_CONTINUE;

		}

		@Override
		public void onGroup(UserData user,Msg msg) {

			Long groupId = telegramIndex.get(msg.chatId());

			if (msg.hasText()) {

				Launcher.TINX.api.sendGroupMsg(groupId,formarMessage(user) + msg.text(),true);

			} else if (msg.sticker() != null) {

				Launcher.TINX.api.sendGroupMsg(groupId,formarMessage(user) + CqCodeUtil.inputSticker(msg.sticker()),false);
				
			} else if (msg.photo() != null) {
				
				Launcher.TINX.api.sendGroupMsg(groupId,formarMessage(user) + CqCodeUtil.makeImage(msg.photo()),false);
				
			}

		}
		
	}

	public static class QQListener extends TinxListener {

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
		public void onGroup(MessageUpdate msg) {

			if (!qqIndex.containsKey(msg.group_id)) return;

			Long chatId = qqIndex.get(msg.group_id);

			if (disable.containsKey(chatId)) return;
			
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

}
