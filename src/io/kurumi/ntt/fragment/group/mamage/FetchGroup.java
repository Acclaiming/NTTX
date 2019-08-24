package io.kurumi.ntt.fragment.group.mamage;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.pengrad.telegrambot.model.ChatMember;
import com.pengrad.telegrambot.request.GetChat;
import com.pengrad.telegrambot.request.GetChatMember;
import com.pengrad.telegrambot.request.LeaveChat;
import com.pengrad.telegrambot.response.GetChatMemberResponse;
import com.pengrad.telegrambot.response.GetChatResponse;
import io.kurumi.ntt.Launcher;
import io.kurumi.ntt.db.GroupData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.BotServer;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.bots.GroupBot;
import io.kurumi.ntt.fragment.group.mamage.FetchGroup;
import io.kurumi.ntt.model.Msg;
import java.util.LinkedList;
import java.util.List;

public class FetchGroup extends Fragment {

	static Log log = LogFactory.get(FetchGroup.class);

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerAdminFunction("_fetch_chats","_fetch_status");

	}

	@Override
	public int checkFunction(UserData user,Msg msg,String function,String[] params) {

		return PROCESS_ASYNC;

	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		msg.send("正在开始刷新 ~").async();

		if (function.endsWith("status")) {

			GroupData.data.saveAll();

			synchronized (GroupData.data.idIndex) {

				List<GroupData> all = GroupData.data.getAll();

				int ok = 0;
				int error = 0;
				int base = 0;
				int remove = 0;
				
				for (int index = 0;index < all.size();index ++) {

					GroupData data = all.get(index);
					
					if (!BotServer.idIndex.containsKey(data.bot)) { error ++;continue; }

					BotFragment originBot = BotServer.idIndex.get(data.bot);
					
					GetChatMemberResponse member = originBot.execute(new GetChatMember(data.id,data.bot.intValue()));

					if (member == null || !member.isOk()) { error ++;continue; }
					
					ChatMember.Status status = member.chatMember().status();

					if (status == ChatMember.Status.administrator) {
						
						data.bot_admin = true;
						
						ok ++;
						
					} else if (status == ChatMember.Status.member) {
						
						data.bot_admin = false;
						
						base ++;
						
					} else {
						
						originBot.execute(new LeaveChat(data.id));
						
						GroupData.data.deleteById(data.id);
						
						remove ++;
						
					}
					
					log.debug("管理 {} 个群组, 非管理 {} 个群组, 退出了 {} 个群组, 出错 {} 个群组, 剩余 {} 个群组.",ok,base,remove,error,all.size() - index - 1);
					
					
				}
				
				msg.send("完成 管理 {} 个群组, 非管理 {} 个群组, 退出了 {} 个群组, 出错 {} 个群组.",ok,base,remove,error).async();
				
			}

			GroupData.data.saveAll();

			
		} else {

			GroupData.data.saveAll();

			LinkedList<Long> failed = new LinkedList<>();

			int success = 0;
			int remove = 0;

			synchronized (GroupData.data.idIndex) {

				List<GroupData> all = GroupData.data.getAll();

				for (int index = 0;index < all.size();index ++) {

					GroupData data = all.get(index);

					if (data.id >= 0) {

						execute(new LeaveChat(data.id));

						GroupData.data.deleteById(data.id);

						remove ++;

						continue;

					}

					// if (data.last != null) continue;

					GetChatResponse chatR = Launcher.INSTANCE.execute(new GetChat(data.id));

					if (chatR != null && chatR.isOk()) {

						GroupData.get(Launcher.INSTANCE,chatR.chat());

						success ++;

					} else if (chatR != null) {

						failed.add(data.id);

					}

					log.debug("群组消息已刷新 {} 条, 失败 {} 条, 剩余 {} 条.",success,failed.size(),all.size() - index - 1); 

				}

				log.debug("剩余 {} 条无效数据",failed.size());

				msg.send("本体刷新了 {} 个群组, 剩余 {} 条数据",success,failed.size()).async();

				success = 0;

				groups:for (Long group : failed) {

					log.debug("非本体群组已刷新 {} 条, 失败 {} 条",success,remove); 

					for (BotFragment bot :  BotServer.fragments.values()) {

						if (!(bot instanceof GroupBot)) continue;

						GetChatResponse chatR = bot.execute(new GetChat(group));

						if (chatR == null && chatR.isOk()) {

							GroupData.get(bot,chatR.chat());

							success ++;

							continue groups;

						}

					}

					remove ++;

					GroupData.data.deleteById(group);

				}

				msg.send("完成 非本体刷新了 {} 个群组, 移除了 {} 条无效数据.",success,remove).async();

			}

			GroupData.data.saveAll();

		}

	}

}
