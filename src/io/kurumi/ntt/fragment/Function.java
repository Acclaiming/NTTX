package io.kurumi.ntt.fragment;

import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.db.UserData;

public abstract class Function extends Fragment {

	private String[] names;
	
	public Function(String... names) {
		
		this.names = names;
		
	}
	
	@Override
	public void init(BotFragment origin) {
		
		super.init(origin);
		
		registerFunction(names);
		
	}

	@Override
	public abstract void onFunction(UserData user,Msg msg,String function,String[] params);
	
}
