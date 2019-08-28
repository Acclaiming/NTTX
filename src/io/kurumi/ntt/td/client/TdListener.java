package io.kurumi.ntt.td.client;

import io.kurumi.ntt.td.TdApi.*;
import io.kurumi.ntt.td.model.TMsg;
import io.kurumi.ntt.td.TdApi;

public class TdListener {
	
	public TdClient client;
	
	public <T extends TdApi.Object> T execute(Function function) throws TdException {
		
		return client.execute(function);
		
	}
	
	public void onPrivateMessage(User user,TMsg message) {
	}
	
	public void onFunction(TMsg message) {
	}
	
}
