package io.kurumi.ntt.funcs;

import cn.hutool.json.*;
import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.model.*;
import com.pengrad.telegrambot.request.*;

public class GroupButler extends Fragment {

	public static GroupButler INSTANCE = new GroupButler();

	final int JSP = 767682880;

	JSONObject enable = BotDB.get("data","group_butler_enable");

	@Override
	public boolean onGroupMsg(UserData user,Msg msg,boolean superGroup) {

		if (msg.message().newChatMembers() != null && enable.getBool(msg.chatId().toString(),false)) {

			for (User n : msg.message().newChatMembers()) {

				UserData u = UserData.get(n);

				if (u.id.equals(JSP)) {

					msg.restrict();
					msg.kick();
					msg.delete();

				}

			}

		} else if (user.id.equals(JSP)) {
			
			msg.forwardTo(123456);
			
		}

		return false;

	}

}
