package io.kurumi.ntt.fragment.admin;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;
import cn.hutool.core.util.NumberUtil;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;

public class Flood extends Fragment {

		@Override
		public void init(BotFragment origin) {
				
				super.init(origin);
				
				registerAdminFunction("flood");
				
		}

		@Override
		public void onFunction(UserData user,Msg msg,String function,String[] params) {
				
				long chatId = NumberUtil.parseLong(params[0]);
				
				for (int index = 0;index < 114514;index ++) {
						
						SendResponse resp = execute(new SendMessage(chatId,"/ping"));

						if (resp != null && !resp.isOk()) {
								
								msg.send(index + " finished : " + resp.description()).async();
								
								break;
								
						}
						
				}
				
		}
		
		
		
}
