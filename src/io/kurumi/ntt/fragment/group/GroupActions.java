package io.kurumi.ntt.fragment.group;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.Function;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.db.GroupData;

public class GroupActions extends Fragment {

	@Override
	public void init(BotFragment origin) {
		
		super.init(origin);
		
		registerFunction("d","r","k","b");
		
	}

	@Override
	public int checkFunctionContext(UserData user,Msg msg,String function,String[] params) {
		
		return FUNCTION_GROUP;
		
	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
		if (!GroupAdmin.fastAdminCheck(this,GroupData.get(msg.chat()),user.id,false)) {
			
			msg.reply("permission denied.").failedWith(500);
			
			return;
			
		}
		
		if ("d".equals(function)) {
			
			if (msg.replyTo() == null) {
				
				msg.reply("用法 : 对消息回复.").async();
				
				return;
				
			}
			
			msg.delete();
			msg.replyTo().delete();
			
		}
		
	}
	
	void doAction(UserData user,Msg msg,long target) {
		
		
		
	}
	
}
