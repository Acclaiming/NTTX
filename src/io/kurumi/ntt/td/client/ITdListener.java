package io.kurumi.ntt.td.client;

import io.kurumi.ntt.td.TdApi;

public interface ITdListener {
	
	public void onInit(TdClient client);
	public void onEvent(TdApi.Object event);
	
}
