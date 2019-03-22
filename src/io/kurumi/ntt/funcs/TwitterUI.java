package io.kurumi.ntt.funcs;

import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.*;
import io.kurumi.nttools.twitter.*;

public class TwitterUI extends Fragment {

	@Override
	public boolean onMsg(UserData user,Msg msg) {
		
		if (!msg.isCommand()) return false;
		
		switch (msg.commandName()) {
			
			case "tauth" : tauth(user,msg);break;
			
			case "trem" : 
			
		}
		
	}
	
	void tauth(UserData user,Msg msg) {
		
		ApiToken.defaultToken.createApi().getOAuthRequestToken();
		
	}
	
}
