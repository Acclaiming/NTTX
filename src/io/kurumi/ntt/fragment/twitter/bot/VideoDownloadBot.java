package io.kurumi.ntt.fragment.twitter.bot;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.BotFragment;

public class VideoDownloadBot extends Fragment {


	
	@Override
	public void init(BotFragment origin) {
		
		super.init(origin);
		
		registerAdminFunction("vdb_init","vdb_unset");
		
	}
	
}
