package io.kurumi.ntt.fragment.bots;

import io.kurumi.ntt.fragment.group.GroupAdmin;
import io.kurumi.ntt.fragment.group.GroupFunction;
import io.kurumi.ntt.fragment.group.GroupOptions;
import io.kurumi.ntt.fragment.group.JoinCaptcha;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;

public class JoinCaptchaBot extends UserBotFragment {

		@Override
		public void reload() {
				
				super.reload();
				
				addFragment(new GroupOptions());
				addFragment(new GroupFunction());
				addFragment(new GroupAdmin());
				addFragment(new JoinCaptcha());
				
		}

		@Override
		public int checkMsg(UserData user,Msg msg) {
				
				if (msg.message().newChatMembers() != null) {
						
						if (me.id().equals(msg.message().newChatMembers()[0].id())) {
								
								msg.send("欢迎使用 :)","在群组发送 /options 即可调出设置。").exec();
								
								return PROCESS_REJECT;
								
						}
						
				}
				
				return PROCESS_SYNC;
				
		}
		
		

}
