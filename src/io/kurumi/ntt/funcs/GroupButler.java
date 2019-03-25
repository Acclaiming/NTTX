package io.kurumi.ntt.funcs;

import cn.hutool.json.*;
import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.model.*;
import com.pengrad.telegrambot.request.*;
import io.kurumi.ntt.*;
import java.security.acl.*;

public class GroupButler extends Fragment {

	public static GroupButler INSTANCE = new GroupButler();

	final int JSP = 767682880;

	JSONObject enable = BotDB.getJSON("data","group_butler_enable",true);

	public void save() {
		
		BotDB.setJSON("data","group_butler_enable",enable);
		
	}
	
	@Override
	public boolean onGroupMsg(UserData user,Msg msg,boolean superGroup) {
		
		if (msg.message().newChatMembers() != null) {
			
			if (enable.getBool(msg.chatId().toString(),false)) {

			for (User n : msg.message().newChatMembers()) {

				UserData u = UserData.get(n);

				if (u.id.equals(JSP)) {

					msg.restrict();
					msg.kick();
					msg.delete();

				} else {
                    
                    
                    
                }

			}
			
			} else if (origin.me.id().equals( msg.message().newChatMembers()[0].id())) {
				
				/*
				
				ChatMember perm = bot().execute(new GetChatMember(msg.chatId(),origin.me.id())).chatMember();

				if (perm.canDeleteMessages() && perm.canRestrictMembers()) {

					enable.put(msg.chatId().toString(),true);

					save();

				}
				
				*/
			
				
			}

		} else if (user.id.equals(JSP)) {

			msg.forwardTo(-1001381253862l);

		} else if (Env.FOUNDER.equals(user.userName) && msg.isCommand()) {

			switch (msg.command()) {

				case "ginit" : {

						ChatMember perm = bot().execute(new GetChatMember(msg.chatId(),origin.me.id())).chatMember();

						if (perm != null && perm.canDeleteMessages() && perm.canRestrictMembers()) {
							
							enable.put(msg.chatId().toString(),true);
							
							save();
							
							msg.send("group inited ...").exec();
							
						} else {
							
							msg.send("no permission ...").exec();
							
						}
						
					} break;
					
					
				case "gdisable" : {
					
						enable.put(msg.chatId().toString(),false);

						save();

						msg.send("disabled ...").exec();
						
					
				} break;

			}

		}

		return false;

	}
    
    /*
    
    void startCaptcha(UserData user,Msg msg,UserData newUser) {
        
        if (!newUser.isBot) {
            
            if (!msg.restrict(newUser.id.intValue())) {
                
                noPerm(user,msg);
                
            } else {
                
                msg.reply("新dalao欢迎来你群 ！现在需要几分钟的时间来确认乃是不是机器人 Σ( ﾟωﾟ","","").buttons();
                
            }
            
        }
        
    }
    
    void noPerm(UserData user,Msg msg) {
        
        msg.send("嘛 本群开启了进群验证 但是机器人的权限被取消了 已关闭加群验证 ~").exec();
        
        enable.put(msg.chatId().toString(),false);
        
        save();
        
    }
    
    */

}
