package io.kurumi.ntt.fragment.bots;

import io.kurumi.ntt.db.Data;
import io.kurumi.ntt.fragment.bots.UserBot;
import java.util.Map;

public class UserBot {
	
	public static Data<UserBot> data = new Data<UserBot>("UserCustomBot",UserBot.class);
	
	public Long id;
	public String userName;
	
	public Long user;
	public String token;
	
	public int type;
	public Map<String,String> params;
	
	public String information() {
		
		StringBuilder information = new StringBuilder();

		if (type == 0) {
			
			String welcomeMsg = params.get("msg");

			information.append("欢迎语 : > ").append(welcomeMsg).append(" <");
			
		}
		
		return information.toString();
		
	}
	
	public String typeName() {
		
		switch (type) {
			
			case 0 : return "转发私聊BOT";
			
			default : return null;
			
		}
		
	}
	
}
