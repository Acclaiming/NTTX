package io.kurumi.ntt.listeners;

import io.kurumi.ntt.td.client.TdBot;
import io.kurumi.ntt.Env;

public class BetaLauncher extends TdBot {
	
	public BetaLauncher() {

		super(Env.BETA_TOKEN);

	}
	
}
