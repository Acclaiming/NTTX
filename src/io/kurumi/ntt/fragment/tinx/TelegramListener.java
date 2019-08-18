package io.kurumi.ntt.fragment.tinx;

import io.kurumi.ntt.Launcher;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.qq.CqCodeUtil;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.fragment.qq.TelegramBridge;
import io.kurumi.ntt.cqhttp.TinxApi;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.utils.NTT;
import io.kurumi.ntt.model.request.ButtonMarkup;
import io.kurumi.ntt.cqhttp.Variants;
import io.kurumi.ntt.cqhttp.response.BaseResponse;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import javax.print.attribute.standard.NumberUp;
import cn.hutool.core.util.NumberUtil;
import io.kurumi.ntt.fragment.BotFragment;

public class TelegramListener extends Fragment {

	public static final String POINT_ACCEPT = "qj_accept";
	public static final String POINT_REJECT = "qj_reject";
	public static final String POINT_BLOCK = "qj_block";
	public static final String POINT_IGNORE = "qj_ignore";

	@Override
	public void init(BotFragment origin) {
		
		super.init(origin);
		
		registerCallback(POINT_ACCEPT,POINT_REJECT,POINT_BLOCK,POINT_IGNORE);
		
	}
	
	@Override
	public void onCallback(UserData user,Callback callback,String point,String[] params) {

		if (NTT.checkGroupAdmin(callback)) return;

		if (POINT_IGNORE.equals(point)) {

			callback.editMarkup(new ButtonMarkup() {{ newButtonLine("已忽略"); }});

			return;

		}
		
		Long groupId = TelegramBridge.telegramIndex.get(callback.chatId());

		InlineKeyboardButton[][] buttons = callback.message().replyMarkup().inlineKeyboard();

		String flag = buttons[0][0].callbackData().split(",")[1];
		
		Long userId  = NumberUtil.parseLong(buttons[0][1].callbackData().split(",")[1]);
		

		TinxApi api = Launcher.TINX.api;

		if (POINT_ACCEPT.equals(point)) {

			api.setGroupAddRequestAsync(flag,Variants.GR_ADD,true,null);

			callback.editMarkup(new ButtonMarkup() {{ newButtonLine("已同意"); }});
			
		} else if (POINT_REJECT.equals(point)) {

			api.setGroupAddRequestAsync(flag,Variants.GR_ADD,false,null);

			callback.editMarkup(new ButtonMarkup() {{ newButtonLine("已拒绝"); }});

		} else if (POINT_BLOCK.equals(point)) {

			api.setGroupAddRequest(flag,Variants.GR_ADD,true,null);
			api.setGroupKickAsync(groupId,userId,true);
			
			callback.editMarkup(new ButtonMarkup() {{ newButtonLine("已屏蔽"); }});

		}

	}

	static String formarMessage(UserData user) {

		String message = user.name() + " : ";

		return message;

	}

	@Override
	public int checkMsg(UserData user,Msg msg) {

		return (msg.isGroup() && !TelegramBridge.disable.containsKey(msg.chatId()) && TelegramBridge.telegramIndex.containsKey(msg.chatId())) ? PROCESS_ASYNC : PROCESS_CONTINUE;

	}

	@Override
	public void onGroup(UserData user,Msg msg) {

		Long groupId = TelegramBridge.telegramIndex.get(msg.chatId());

		TinxApi api = Launcher.TINX.api;

		if (msg.hasText()) {

			api.sendGroupMsgAsync(groupId,formarMessage(user) + msg.text(),true);

		} else if (msg.sticker() != null) {

			api.sendGroupMsgAsync(groupId,formarMessage(user) + CqCodeUtil.inputSticker(msg.sticker()),false);

		} else if (msg.photo() != null) {

			api.sendGroupMsgAsync(groupId,formarMessage(user) + CqCodeUtil.makeImage(msg.photo()),false);

		}

	}

}
