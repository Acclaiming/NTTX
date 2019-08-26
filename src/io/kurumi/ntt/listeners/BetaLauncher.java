package io.kurumi.ntt.listeners;

import cn.hutool.core.util.StrUtil;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.td.TdApi.*;
import io.kurumi.ntt.td.client.TdBot;
import io.kurumi.ntt.td.model.TdMessage;

public class BetaLauncher extends TdBot {

	public BetaLauncher() {

		super(Env.BETA_TOKEN);

	}

	@Override
	public void onFunction(TdMessage msg,String function,String[] params) {
		
		if (msg.message.chatId > 0) {
			
			send(msg.send(plainText("喵")));
			
		}
		
		if ("ping".equals(function)) {
			
			send(msg.send(plainText("pong")));
			
		}
		
	}
	
	InputMessageText plainText(String text) {
		
		FormattedText format = new FormattedText(text,new TextEntity[0]);
		
		return new InputMessageText(format,true,true);
		
	}

}
