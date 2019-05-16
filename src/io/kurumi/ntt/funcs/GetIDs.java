package io.kurumi.ntt.funcs;

import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.db.*;

public class GetIDs extends Fragment {

	public static GetIDs INSTANCE = new GetIDs();
	
	@Override
	public boolean onMsg(UserData user,Msg msg) {
		
		if ("id".equals(msg.command())) {
			
			if (msg.isReply()) {
				
				msg.send(msg.replyTo().from().id.toString()).publicFailed();
				
				} else {
			
			msg.send(msg.chatId().toString()).publicFailed();
			
			}
			
			return true;
			
		}
		
		return false;
		
	}

	@Override
	public boolean onChanPost(UserData user,Msg msg) {
		
		return onMsg(user,msg);
		
	}

}
