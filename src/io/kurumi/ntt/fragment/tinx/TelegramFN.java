package io.kurumi.ntt.fragment.tinx;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.db.UserData;
import cn.hutool.core.util.NumberUtil;
import io.kurumi.ntt.fragment.qq.TelegramBridge;
import io.kurumi.ntt.utils.NTT;

public class TelegramFN extends Fragment {

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerFunction("tinx_enable","tinx_disable");

	}

	@Override
	public int checkFunctionContext(UserData user,Msg msg,String function,String[] params) {
		
		return FUNCTION_GROUP;
		
	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

	 if (function.endsWith("_enable")) {

			if (!TelegramBridge.telegramIndex.containsKey(msg.chatId())) {

				msg.send("本群没有开启QQ群组消息同步, 请联系机器人管理者.").async();

				return;

			}

			if (NTT.checkGroupAdmin(msg)) return;

		 if (!TelegramBridge.disable.containsKey(msg.chatId())) {

				msg.send("没有关闭 :)").async();

			} else {

			 TelegramBridge.disable.remove(msg.chatId());

			 TelegramBridge.GroupBind bind = TelegramBridge.data.getById(msg.chatId());

				bind.disable = null;

			 TelegramBridge.data.setById(bind.id,bind);

				msg.send("已开启 :) 使用 /tinx_disable 关闭.").async();

				return;

			}

		} else if (function.endsWith("_disable")) {

			if (!TelegramBridge.telegramIndex.containsKey(msg.chatId())) {

				msg.send("本群没有开启QQ群组消息同步, 请联系机器人管理者.").async();

				return;

			}

			if (NTT.checkGroupAdmin(msg)) return;

			if (TelegramBridge.disable.containsKey(msg.chatId())) {

				msg.send("没有开启 :)").async();

			} else {

				TelegramBridge.disable.put(msg.chatId(),true);

				TelegramBridge.GroupBind bind = TelegramBridge.data.getById(msg.chatId());

				bind.disable = true;

				TelegramBridge.data.setById(bind.id,bind);

				msg.send("已关闭 :) 使用 /tinx_enable 重新开启.").async();

				return;

			}

		}

	}
}
