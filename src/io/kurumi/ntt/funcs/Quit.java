package io.kurumi.ntt.funcs;

import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.*;
import com.pengrad.telegrambot.request.*;

public class Quit extends Fragment {

	public static Quit INSTANCE = new Quit();
	
	@Override
	public boolean onGroupMsg(UserData user,Msg msg,boolean superGroup) {
		
		if (user.developer() && "quit".equals(msg.command())) {
			
			bot().execute(new LeaveChat(msg.chatId()));
			
			return true;
			
		}
		
		return false;
		
    }
	
}
