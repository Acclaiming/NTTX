package io.kurumi.ntt.listeners.base;

import io.kurumi.ntt.td.TdApi.User;
import io.kurumi.ntt.td.client.TdFunction;
import io.kurumi.ntt.td.model.TMsg;

public class TdPingFunction extends TdFunction {

	@Override
	public String functionName() {
	
		return "ping";
		
	}
	
	@Override
	public void onFunction(User user,TMsg msg,String function,String[] params) {
		
		send(chatId(msg.chatId).input(inputText(text("喵..."))));
		
	}
	
}
