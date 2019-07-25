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

	@Override
	public int checkMsg(UserData user,Msg msg) {

		if (msg.message().newChatMembers() != null) {

			if (me.id().equals(msg.message().newChatMembers()[0].id())) {

				return PROCESS_SYNC;

			}

		}

		return PROCESS_REJECT;

	}

	@Override
	public void onMsg(UserData user,Msg msg) {

		msg.send("欢迎使用 :)","在群组发送 /options 即可调出设置。").async();

	}

	
}
