package io.kurumi.ntt.fragment.qq;

import cn.hutool.core.util.NumberUtil;
import io.kurumi.ntt.db.Data;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import java.util.HashMap;
import io.kurumi.ntt.Launcher;
import io.kurumi.ntt.utils.Html;

public class BindGroup extends Fragment {

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerAdminFunction("tinx_bind","tinx_unbind","tinx_list");

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
			groupIndex.put(bind.groupId,bind.id);

			data.setById(bind.id,bind);

			msg.send("完成 :)").async();

		}

	}

	@Override
	public int checkMsg(UserData user,Msg msg) {

		return msg.isGroup() && telegramIndex.containsKey(msg.chatId()) ? PROCESS_ASYNC : PROCESS_CONTINUE;

	}

	@Override
	public void onGroup(UserData user,Msg msg) {
		
		if (msg.hasText()) {

			Launcher.TINX.api.sendGroupMsg(telegramIndex.get(msg.chatId()),formarMessage(user) + msg.text(),true);

		} else if (msg.sticker() != null) {
			
			Launcher.TINX.api.sendGroupMsg(telegramIndex.get(msg.chatId()),formarMessage(user) + msg.sticker().emoji(),true);
			
		}

	}

	static String formarMessage(UserData user) {
		
		String message = user.name() + " : ";
		
		return message;
		
	}

}
