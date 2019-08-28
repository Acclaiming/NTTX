package io.kurumi.ntt.td.client;

import io.kurumi.ntt.td.TdApi;
import io.kurumi.ntt.td.TdApi.Function;
import io.kurumi.ntt.td.TdApi.User;
import io.kurumi.ntt.td.model.TMsg;

public class TdListener {
	
	public TdClient client;
	
	public <T extends TdApi.Object> T execute(Function function) throws TdException {
		
		return client.execute(function);
		
	}
	
	public void onMessage(User user,TMsg message) {
		
		
		
	}
	
	public void onFunction(User user,TMsg message) {
	}
	
}
