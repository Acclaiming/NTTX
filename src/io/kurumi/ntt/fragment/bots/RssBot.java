package io.kurumi.ntt.fragment.bots;

import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.fragment.rss.*;

public class RssBot extends UserBotFragment {

	@Override
	public void reload() {
	
		super.reload();
		
		addFragment(new RssSub());
		
	}

	@Override
	public boolean msg() {

		return true;

	}

}
