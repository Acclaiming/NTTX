package io.kurumi.ntt.fragment.debug;

import io.kurumi.ntt.funcs.abs.*;
import io.kurumi.ntt.model.*;
import java.util.*;
import io.kurumi.ntt.db.*;

public class DebugMsg extends Function {

	@Override
	public void functions(LinkedList<String> names) {
		
		names.add("get_msg");
		
	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		if (msg.replyTo() == null) {
			
			msg.send("没有对消息回复").exec();
			
			return;
			
		}
		
		msg.send(msg.replyTo().message().toString()).exec();
		
	}
	
}
