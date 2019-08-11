package io.kurumi.ntt.fragment.twitter.tasks;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.BotFragment;
import java.util.TimerTask;
import io.kurumi.ntt.fragment.twitter.TAuth;

public class NameUpdateTask extends TimerTask {

	@Override
	public void run() {
		
		for (TAuth account : TAuth.data.getAllByField("anu",true)) {}
		
	}

}
