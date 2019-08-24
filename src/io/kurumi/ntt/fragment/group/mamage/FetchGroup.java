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
import io.kurumi.ntt.fragment.BotServer;
import io.kurumi.ntt.fragment.bots.GroupBot;

public class FetchGroup extends Fragment {

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerAdminFunction("_group_fetch");

	}

	@Override
	public int checkFunction(UserData user,Msg msg,String function,String[] params) {
		
		return PROCESS_ASYNC;
		
	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		msg.send("正在开始刷新 ~").async();
		
		GroupData.data.saveAll();

		LinkedList<Long> failed = new LinkedList<>();

		int success = 0;
		int remove = 0;
		
		synchronized (GroupData.data.idIndex) {

			for (GroupData data : GroupData.data.getAll()) {

				if (data.id >= 0) {

					execute(new LeaveChat(data.id));

					GroupData.data.deleteById(data.id);

					remove ++;
					
					return;

				}

				// if (data.last != null) continue;

				GetChatResponse chatR = Launcher.INSTANCE.execute(new GetChat(data.id));

				if (chatR.isOk()) {

					GroupData.get(Launcher.INSTANCE,chatR.chat());

					success ++;
					
				} else {

					failed.add(data.id);

				}

			}

		}
		
		msg.send("本体刷新了 {} 个群组, 剩余 {} 条数据",success,failed.size()).async();
		
		success = 0;

		groups:for (Long group : failed) {

			for (BotFragment bot :  BotServer.fragments.values()) {

				if (!(bot instanceof GroupBot)) continue;

				GetChatResponse chatR = bot.execute(new GetChat(group));

				if (chatR.isOk()) {

					GroupData.get(bot,chatR.chat());

					success ++;
					
					continue groups;

				}

			}
			
			remove ++;

			GroupData.data.deleteById(group);

		}

		msg.send("完成 非本体刷新了 {} 个群组, 移除了 {} 条无效数据.",success,remove).async();

		GroupData.data.saveAll();
		
	}

}
