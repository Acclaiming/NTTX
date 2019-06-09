package io.kurumi.ntt.funcs.admin;

import io.kurumi.ntt.db.Data;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.funcs.abs.Function;
import io.kurumi.ntt.model.Msg;
import java.util.LinkedList;
import com.pengrad.telegrambot.model.Update;
import io.kurumi.ntt.Env;
import cn.hutool.core.util.NumberUtil;
import com.pengrad.telegrambot.request.KickChatMember;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.request.UnbanChatMember;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.ForwardMessage;

public class Firewall extends Function {

	public static class Id {
		
		public long id;
		
		public Id() {}
		public Id(long id) { this.id = id; }
		
	}
	
	public static Data<Id> block = new Data<Id>("UserBlock",Id.class);
	
	@Override
	public void functions(LinkedList<String> names) {
		
		names.add("accept");
		names.add("drop");
		
	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
		if (!user.developer()) {
			
			msg.send("Permission Denied").exec();
			
			return;
			
		}
		
		if (params.length == 0) {
			
			msg.send("invlid params").exec();
		
			return;
			
		}
		
		long target = -1;
		
		if (NumberUtil.isNumber(params[0])) {
			
			target = NumberUtil.parseLong(params[0]);
			
		} else {
			
			UserData.data.getByField("userName",params[0]);
			
		}
		
		if (target == -1) {
			
			msg.send("无记录").exec();
			
			return;
			
		}
		
		boolean exists = block.containsId(target);
		
		if ("accept".equals(function)) {
			
			if (exists) {
				
				block.deleteById(target);
				
				msg.send("removed block").exec();
				
			} else {
				
				msg.send("not blocked").exec();
				
			}
			
		} else {
			
			if (exists) {
				
				msg.send("already blocked").exec();
				
			} else {
				
				block.setById(target,new Id(target));

				msg.send("blocked").exec();
				
			}
			
		}
		
	}

	@Override
	public boolean onMsg(UserData user,Msg msg) {
		
		if (super.onMsg(user,msg)) return true;
		
		if (user.developer() && msg.isStartPayload()) {
			
			String[] payload = msg.payload();

			if ("accept".equals(payload[0]) || "drop".equals(payload[0])) {
				
				if (payload.length < 2) {

					msg.send("invlid params").exec();

					return true;

				}

				UserData target  = UserData.get(NumberUtil.parseLong(payload[1]));

				if (target.developer()) {

					msg.send("不可以").exec();

					return true;

				}

				boolean exists = block.containsId(target.id);

				if ("accept".equals(payload[0])) {

					if (exists) {

						block.deleteById(target.id);

						msg.send("removed block").exec();

					} else {

						msg.send("not blocked").exec();

					}

				} else {

					if (exists) {

						msg.send("already blocked").exec();

					} else {

						block.setById(target.id,new Id(target.id));

						msg.send("blocked").exec();

					}

				}
				
				return true;
				
			}
			
		}
			
			return false;
		
	}

	@Override
	public boolean onUpdate(UserData user,Update update) {
		
		if (user != null && !user.developer() && block.containsId(user.id)) {
			
			return true;
			
			/*
			
			if (update.message() != null) {
				
				Message msg = update.message();
				
				bot().execute(new DeleteMessage(msg.chat().id(),user.id.intValue()));

				if (msg.chat().type() != Chat.Type.Private) {
					
					bot().execute(new KickChatMember(msg.chat().id(),user.id.intValue()));
					// bot().execute(new UnbanChatMember(msg.chat().id(),msg.newChatMember().id().intValue()));
					
					bot().execute(new ForwardMessage(Env.GROUP,msg.chat().id(),msg.messageId()));
					
				}
				
				//new Msg(this,update.message()).forwardTo(Env.GROUP);
				
			}
			
			return true;
			
			*/
			
		}
		
		return false;
		
	}
	
}
