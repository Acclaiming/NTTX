package io.kurumi.ntt.fragment.base;

import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.db.*;

public class UserExport extends Fragment {

	@Override
	public void init(BotFragment origin) {

		super.init(origin);
		
		registerFunction("user");
		
		registerPayload("user");
		
	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
		
		
	}
	
}
