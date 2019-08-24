package io.kurumi.ntt.fragment.group.mamage;

import io.kurumi.ntt.db.GroupData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.Launcher;
import java.util.LinkedList;

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

				if (data.last != null) continue;
				
				
				
			}

		}

	}

}
