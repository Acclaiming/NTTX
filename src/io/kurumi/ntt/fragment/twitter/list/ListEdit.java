package io.kurumi.ntt.fragment.twitter.list;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.db.UserData;

public class ListEdit extends Fragment {

	final String POINT_OPEN = "list_open";
	
	@Override
	public void init(BotFragment origin) {
		
		super.init(origin);
		
		registerFunction("csv");
		
		registerPoint(POINT_OPEN);
		
	}
	
	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
	}
	
}
