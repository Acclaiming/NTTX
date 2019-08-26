package io.kurumi.ntt.fragment.td;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.td.client.TdBot;
import io.kurumi.ntt.td.TdApi;
import io.kurumi.ntt.td.client.TdException;

public class TdTest extends Fragment {

	@Override
	public void init(BotFragment origin) {
	
		super.init(origin);
		
		registerAdminFunction("test_get_members");
		
	}

	@Override
	public int checkFunctionContext(UserData user,Msg msg,String function,String[] params) {
		
		return FUNCTION_PUBLIC;
		
	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
		TdBot bot = new TdBot(origin.getToken());
		
		bot.start();
		
		try {
			
			TdApi.ChatMembers members = bot.execute(new TdApi.GetSupergroupMembers((int)(msg.chatId() / -100L),new TdApi.SupergroupMembersFilterRecent(),0,200));
			
			String message = "所有用户 : \n";

			for (TdApi.ChatMember member : members.members) {

				message += "\n" + member.userId;

			}

			msg.send(message).async();
			
			// bot.destroy();
			
		} catch (TdException e) {}
		
		// 
		
		
	}
	
}
