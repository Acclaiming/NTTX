package io.kurumi.ntt.fragment.tinx;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.db.UserData;

public class TinxManager extends Fragment {

	@Override
	public void init(BotFragment origin) {
		
		super.init(origin);
		
		registerAdminFunction("tinx");
		
	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
	
		if (params.length < 0) {
			
			msg.invalidParams("action","...").async();
			
			return;
			
		}
	
	}
	
}
