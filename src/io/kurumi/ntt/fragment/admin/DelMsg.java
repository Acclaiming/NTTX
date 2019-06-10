package io.kurumi.ntt.fragment.admin;

import com.pengrad.telegrambot.request.DeleteMessage;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.fragment.abs.request.Send;

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
			
			if (msg.params().length == 2) {
				
				bot().execute(new DeleteMessage(chatId,Integer.parseInt(msg.params()[1])));
				
			}
				
			
			Msg send = new Send(this,chatId,"Test").send();

			if (send != null) {
				
				send.delete();
				
				Msg status = msg.send("正在删除...").send();

				for (int index = 1;index < send.messageId();index ++) {
					
					bot().execute(new DeleteMessage(chatId,send.messageId() - index));
					
					status.edit("正在删除 剩余 : " + (send.messageId() - index - 1)).exec();
					
				}
				
			}
			
		}
		
		return true;
		
	}
	
	
}
