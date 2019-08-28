package io.kurumi.ntt.listeners.group;

import io.kurumi.ntt.td.client.TdFunction;
import io.kurumi.ntt.td.TdApi.*;
import io.kurumi.ntt.td.model.TMsg;
import io.kurumi.ntt.td.client.TdInterface.TextBuilder;
import io.kurumi.ntt.td.client.TdException;
import io.kurumi.ntt.td.client.TdListener;
import cn.hutool.core.util.StrUtil;
import java.util.LinkedList;
import io.kurumi.ntt.td.TdApi;
import cn.hutool.log.StaticLog;
import io.kurumi.ntt.Launcher;

public class CleanDeleteAccount extends TdListener {

	@Override
	public void init() {

		registerFunction("cls_da");

	}

	@Override
	public void onFunction(User user,TMsg msg,String function,String[] params) {

		if (msg.isPrivate()) {

			send(msg.sendText(text(getLocale(user).FN_PUBLIC_ONLY)));

			return;

		} 

		if (!msg.isChannel()) {

			ChatMember chatMember = execute(new GetChatMember(msg.chatId,user.id));

			if (!(chatMember.status instanceof ChatMemberStatusCreator || chatMember.status instanceof ChatMemberStatusAdministrator)) {

				sendText(msg,getLocale(user).NOT_CHAT_ADMIN);

				return;

			}

		}

		int size;

		LinkedList<Integer> deletedAccounts = new LinkedList<>();

		if (msg.isBasicGroup()) {

			BasicGroupFullInfo info = execute(new GetBasicGroupFullInfo(msg.groupId));

			size = info.members.length;

			for (ChatMember memberId : info.members) {

				User member = execute(new GetUser(memberId.userId));

				if (member.type instanceof UserTypeDeleted) {

					deletedAccounts.add(member.id);

				}

			}

		} else {

			SupergroupFullInfo group = execute(new GetSupergroupFullInfo(msg.groupId));

			size = group.memberCount;

			for (int index = 0;index < size;index += 200) {

				ChatMembers members = null;

				try {

					members = execute(new GetSupergroupMembers(msg.groupId,new SupergroupMembersFilterRecent(),index,200));

				} catch (TdException e) {

					try {

						members = execute(new GetSupergroupMembers(msg.groupId,new SupergroupMembersFilterRecent(),index,200));

						// TDLib 第一次取 会出错

					} catch (TdException ex) {
					}

				}

				for (ChatMember memberId : members.members) {

					User member = execute(new GetUser(memberId.userId));

					if (member.type instanceof UserTypeDeleted) {

						deletedAccounts.add(member.id);

					}

				}

			}

			if (deletedAccounts.isEmpty()) {

				sendText(msg,getLocale(user).DA_NOT_FOUND);

				return;

			} else {

				sendText(msg,getLocale(user).DA_FOUND,deletedAccounts.size());

			}

			long start = System.currentTimeMillis();

			for (Integer deleted : deletedAccounts) {

				send(new SetChatMemberStatus(msg.chatId,deleted,new ChatMemberStatusLeft()));

			}
			
			sendText(msg,getLocale(user).DA_FINISH,(System.currentTimeMillis() - start) / 1000);

		}

	}

}
