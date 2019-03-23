package io.kurumi.ntt.funcs;

import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.*;
import cn.hutool.json.*;

public class GroupButler extends Fragment {

	public static GroupButler INSTANCE = new GroupButler();
	
	final long JSP = 767682880;
	
	JSONObject enable = BotDB.get("data","group_butler_enable");
	
	@Override
	public boolean onGroupMsg(UserData user,Msg msg,boolean superGroup) {

		if (msg.message().newChatMembers() != null && enable.getBool(msg.chatId().toString(),false)) {
			
			
			
		}
		
		return false;
		
	}
	
}
