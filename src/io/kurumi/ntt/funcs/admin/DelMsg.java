package io.kurumi.ntt.funcs.admin;

import io.kurumi.ntt.funcs.abs.*;
import java.util.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.*;

public class DelMsg extends Function {

	@Override
	public void functions(LinkedList<String> names) {
		
		names.add("del");
		
	}

	@Override
	public int target() {
		
		return Group;
		
	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
	
		if (!user.developer()) return;
		
		msg.delete();
		
		if (msg.isReply()) {
			
			msg.replyTo().delete();
			
		}
		
	}
	
	
}
