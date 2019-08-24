package io.kurumi.ntt.fragment.group.mamage;

import io.kurumi.ntt.db.GroupData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.Launcher;
import java.util.LinkedList;
import com.pengrad.telegrambot.request.GetChat;
import com.pengrad.telegrambot.response.GetChatResponse;
import com.pengrad.telegrambot.request.LeaveChat;

public class FetchGroup extends Fragment {

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerAdminFunction("_group_fetch");

	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		GroupData.data.saveAll();

		LinkedList<Long> failed = new LinkedList<>();
		
		synchronized (GroupData.data.idIndex) {

			for (GroupData data : GroupData.data.getAll()) {

				if (data.id > 0) {
					
					execute(new LeaveChat(data.id));
					
					GroupData.data.deleteById(data.id);
					
					return;
					
				}
				
				if (data.last != null) continue;
				
				GetChatResponse chatR = Launcher.INSTANCE.execute(new GetChat(data.id));

				if (chatR.isOk()) {
					
					GroupData.get(Launcher.INSTANCE,chatR.chat());
					
				} else {
					
					failed.add(data.id);
					
				}
				
			}

		}

	}

}
