package io.kurumi.ntt;

import io.kurumi.ntt.td.client.TdBot;
import io.kurumi.ntt.listeners.base.TdPingFunction;

public class TdLauncher extends TdBot {
	
	public TdLauncher(String botToken) {
		
		super(botToken);
		
	}

	@Override
	public void init() {
		
		addListener(new TdPingFunction());
		
	}
	
}
