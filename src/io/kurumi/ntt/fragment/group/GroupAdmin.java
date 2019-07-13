package io.kurumi.ntt.fragment.group;

import com.pengrad.telegrambot.request.GetChatAdministrators;
import com.pengrad.telegrambot.response.GetChatAdministratorsResponse;
import io.kurumi.ntt.db.GroupData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.Html;
import io.kurumi.ntt.utils.NTT;
import java.util.ArrayList;
import java.util.HashMap;
import com.pengrad.telegrambot.model.ChatMember;
import io.kurumi.ntt.Launcher;

public class GroupAdmin extends Fragment {
		
		public static HashMap<Long,Long> lastUpdate = new HashMap<>();

		public static boolean fastAdminCheck(GroupData data,long userId) {
				
				if (data.admins == null || !lastUpdate.containsKey(data.id) || lastUpdate.get(data.id) - System.currentTimeMillis() > 30 * 60 * 1000) {
						
						GetChatAdministratorsResponse resp = Launcher.INSTANCE.execute(new GetChatAdministrators(data.id));

						if (!resp.isOk()) return false;
						
						data.admins = new ArrayList<>();

						for (ChatMember admin : resp.administrators()) {

								if (admin.user().firstName().equals("")) {

										// 死号

										continue;

								}

								data.admins.add(admin.user().id());

						}
						
						lastUpdate.put(data.id,System.currentTimeMillis());
				
				}
				
				return data.admins.contains(userId);
				
		}
		
		@Override
		public int checkFunctionContext(UserData user,Msg msg,String function,String[] params) {
				
				return FUNCTION_GROUP;
				
		}

		@Override
		public void onFunction(UserData user,Msg msg,String function,String[] params) {
				
				if (NTT.checkGroupAdmin(msg)) return;
		
				GetChatAdministratorsResponse resp = execute(new GetChatAdministrators(msg.chatId()));

				if (!resp.isOk()) {
						
						msg.send("更新缓存失败 :",resp.description()).failedWith();
						
						return;
						
				}
				
				GroupData data = GroupData.get(msg.chat());

				data.admins = new ArrayList<>();
				
				for (ChatMember admin : resp.administrators()) {
						
						if (admin.user().firstName().equals("")) {
								
								// 死号
								
								continue;
								
						}
						
						data.admins.add(admin.user().id());
						
				}
				
				msg.send("管理员缓存更新完成！").exec();
				
		}
		
		
		
}
