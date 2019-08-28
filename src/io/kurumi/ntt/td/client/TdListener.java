package io.kurumi.ntt.td.client;

import io.kurumi.ntt.td.TdApi.*;
import io.kurumi.ntt.td.model.TMessage;
import io.kurumi.ntt.td.TdApi;

public class TdListener {
	
	public TdClient client;
	
	public <T extends TdApi.Object> T execute(Function function) throws TdException {
		
		return client.execute(function);
		
	}
	
	public void onMessage(TMessage message) {
	}
	
	public void onFunction(TMessage message) {
	}
	
}
