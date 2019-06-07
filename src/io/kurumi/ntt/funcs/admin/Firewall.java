package io.kurumi.ntt.funcs.admin;

import io.kurumi.ntt.db.Data;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.funcs.abs.Function;
import io.kurumi.ntt.model.Msg;
import java.util.LinkedList;
import com.pengrad.telegrambot.model.Update;
import io.kurumi.ntt.Env;
import cn.hutool.core.util.NumberUtil;

public class Firewall extends Function {

	public static class Id {
		
		public long id;
		
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
		
		UserData target = null;
		
		if (NumberUtil.isNumber(params[0])) {
			
			target = UserData.get(NumberUtil.parseLong(params[0]));
			
		} else {
			
			UserData.data.getByField("userName",params[0]);
			
		}
		
		if (target == null || target.developer()) {
			
			msg.send("无记录").exec();
			
			return;
			
		}
		
		boolean exists = block.containsId(target.id);
		
		if ("accept".equals(function)) {
			
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
		
	}

	@Override
	public boolean onUpdate(UserData user,Update update) {
		
		if (!user.developer() && block.containsId(user.id)) {
			
			if (update.message() != null) {
				
				//new Msg(this,update.message()).forwardTo(Env.GROUP);
				
			}
			
			return true;
			
		}
		
		return false;
		
	}
	
}
