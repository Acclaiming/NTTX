package io.kurumi.ntt.listeners.base;

import io.kurumi.ntt.td.client.TdListener;
import io.kurumi.ntt.td.TdApi.*;
import io.kurumi.ntt.td.model.TMsg;

public class TdPingFunction extends TdListener {

	@Override
	public void init() {
		
		registerFunction("ping");
		
	}
	
	@Override
	public void onFunction(User user,TMsg msg,String function,String[] params) {
		
		send(chatId(msg.chatId).input(text("喵...")));
		
	}
	
}
