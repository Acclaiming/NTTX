package io.kurumi.ntt.fragment.channel;

import io.kurumi.ntt.funcs.abs.*;
import io.kurumi.ntt.model.*;
import java.util.*;
import io.kurumi.ntt.db.*;

public class Channel extends Function {

	@Override
	public void functions(LinkedList<String> names) {
		// TODO: Implement this method
	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		// TODO: Implement this method
	}

	@Override
	public boolean onChanPost(UserData user,Msg msg) {
		
		if ("get_user_info".equals(msg.command())) {
			
			msg.send(user == null ? "null" : user.userName()).html().exec();
			
			return true;
			
		}
		
		return false;
		
	}
	
}
