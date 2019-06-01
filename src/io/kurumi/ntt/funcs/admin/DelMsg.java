package io.kurumi.ntt.funcs.admin;

import io.kurumi.ntt.db.*;
import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.model.request.*;
import com.pengrad.telegrambot.request.*;

public class DelMsg extends Fragment {

	@Override
	public boolean onMsg(UserData user,Msg msg) {
		
		if (!"del".equals(msg.command())) return false;
		
		if (!user.developer()) return false;
		
		msg.delete();
		
		if (msg.isReply()) {
			
			msg.replyTo().delete();
			
		}
		
		if (msg.params().length > 0) {
			
			long chatId = Long.parseLong(msg.params()[0]);
			
			Msg send = new Send(chatId,"Test").send();

			if (send != null) {
				
				send.delete();
				
				Msg status = msg.send("正在删除...").send();

				for (int index = 1;index < send.messageId();index ++) {
					
					bot().execute(new DeleteMessage(chatId,send.messageId() - index));
					
					status.edit("正在删除 剩余 : " + (send.messageId() - index)).exec();
					
				}
				
			}
			
		}
		
		return true;
		
	}
	
	
}
