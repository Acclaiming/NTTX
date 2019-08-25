package io.kurumi.ntt.fragment.td;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.db.UserData;
import it.ernytech.tdbot.BotClient;
import it.ernytech.tdlib.TdApi;
import it.ernytech.tdlib.Response;
import it.ernytech.tdlib.TdApi.SupergroupFullInfo;

public class TdTest extends Fragment {

	@Override
	public void init(BotFragment origin) {
	
		super.init(origin);
		
		// registerAdminFunction("test_get_members");
		
	}

	@Override
	public int checkFunctionContext(UserData user,Msg msg,String function,String[] params) {
		
		return FUNCTION_PUBLIC;
		
	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
		BotClient bot = new BotClient(origin.getToken());
		
		bot.useChatInfoDatabase(false);
		bot.useMessageDatabase(false);
		bot.apiId(971882);
		bot.apiHash("1232533dd027dc2ec952ba91fc8e3f27");
		
		bot.create();
		
		Response result = bot.execute(new TdApi.GetSupergroupMembers((int)(msg.chatId() / -100L),new TdApi.SupergroupMembersFilterRecent(),0,200));

		TdApi.ChatMembers members = (TdApi.ChatMembers) result.getObject();

		String message = "所有用户 : \n";
		
		for (TdApi.ChatMember member : members.members) {
			
			message += "\n" + member.userId;
			
		}
		
		msg.send(message).async();
		
		bot.close();
		
		
	}
	
}
