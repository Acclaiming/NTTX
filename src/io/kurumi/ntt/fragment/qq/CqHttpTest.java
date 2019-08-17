package io.kurumi.ntt.fragment.qq;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.cqhttp.TinxListener;
import io.kurumi.ntt.cqhttp.update.Update;
import io.kurumi.ntt.cqhttp.update.MessageUpdate;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.cqhttp.TinxBot;
import io.kurumi.ntt.cqhttp.response.GetGroupListResponse;

public class CqHttpTest extends Fragment {

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerFunction("cqhttp");

	}

	@Override
	public void onFunction(UserData user,final Msg msg,String function,String[] params) {

		TinxBot bot = new TinxBot("ws://127.0.1.1:5701/event/","http://127.0.0.1:5700/");

		GetGroupListResponse list = bot.api.getGroupList();

		if (!list.isOk()) {
			
			msg.send("get_group_list failed : " +list.retcode).async();
			
			return;
			
		}
		
		String message = "所有群组 :\n";
		
		for (GetGroupListResponse.Group group : list.data) {
			
			message += "\n" + group.group_name;
			
		}
		
		msg.send(message).async();
		
		bot.addListener(new TinxListener() {

				@Override
				public void onGroup(MessageUpdate groupMsg) {

					msg.send(groupMsg.sender.nickname + " : " + groupMsg.raw_message).async();

				}

			});

		bot.start();

	}
	
}
