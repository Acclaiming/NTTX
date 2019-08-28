package io.kurumi.ntt.listeners.base;

import io.kurumi.ntt.td.TdApi.*;
import io.kurumi.ntt.td.client.TdFunction;
import io.kurumi.ntt.td.model.TMsg;

public class TdGetIdFunction extends TdFunction {

	@Override
	public String functionName() {

		return "id";
	}

	@Override
	public void onFunction(User user,TMsg msg,String function,String[] params) {

		send(chatId(msg.chatId).input(text("CID : ").code(msg.chatId + "").text("\nUID : ").code(user.id + ""))));

	}

}
