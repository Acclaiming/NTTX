package io.kurumi.ntt.fragment.tinx;

import io.kurumi.ntt.Launcher;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.qq.CqCodeUtil;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.fragment.qq.TelegramBridge;
import io.kurumi.ntt.cqhttp.TinxApi;

public class TelegramListener extends Fragment {
	
	static String formarMessage(UserData user) {

		String message = user.name() + " : ";

		return message;

	}

	@Override
	public int checkMsg(UserData user,Msg msg) {

		return msg.isGroup() && !TelegramBridge.disable.containsKey(msg.chatId()) && TelegramBridge.telegramIndex.containsKey(msg.chatId()) ? PROCESS_ASYNC : PROCESS_CONTINUE;

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
