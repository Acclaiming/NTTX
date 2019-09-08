package io.kurumi.ntt.fragment.twitter.list;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.fragment.twitter.TAuth;

public class FriendsClean extends Fragment {

	@Override
	public void init(BotFragment origin) {
		
		origin.init(origin);
		
		registerFunction("clean_friends");
		
	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
		requestTwitter(user,msg);
		
	}

	@Override
	public void onTwitterFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {
	}
	
}
