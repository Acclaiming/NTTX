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

public class CleanDeleteAccount extends TdListener {

	@Override
	public void init() {

		registerFunction("cls_da");

	}

	@Override
	public void onFunction(User user,TMsg msg,String function,String[] params) {
		
		if (msg.isPrivate()) {
			
			send(chatId(msg.chatId).input(inputText(text("你不能清理你自己..."))));
		
			return;
			
		} 
		
		TMsg status = execute(chatId(msg.chatId).replyToMessageId(msg.messageId).input(inputText(text("正在查找..."))));
		
		send(chatId(msg.chatId).input(inputText(text("messageId : " + status.messageId))));
		
		if (msg.isBasicGroup()) {

			BasicGroupFullInfo info = execute(new GetBasicGroupFullInfo(msg.groupId));

			LinkedList<Integer> deletedAccounts = new LinkedList<>();

			for (ChatMember memberId : info.members) {

				User member = execute(new GetUser(memberId.userId));

				if (member.type instanceof UserTypeDeleted) {

					deletedAccounts.add(member.id);

				}

			}

			if (deletedAccounts.isEmpty()) {

				send(status.editText(text("没有找到 DA ...")));
				
				return;

			} else {

				send(status.editText(text("发现 " + deletedAccounts.size() + " 个 DA, 正在爆破 ...")));

			}

			long start = System.currentTimeMillis();

			for (Integer deleted : deletedAccounts) {

				send(new SetChatMemberStatus(msg.chatId,deleted,new ChatMemberStatusLeft()));
				
			}

			send(chatId(msg.chatId).input(inputText(text("清理完成 : 耗时 " + ((System.currentTimeMillis() - start) / 1000) + "s"))));

		} else {

			SupergroupFullInfo group = execute(new GetSupergroupFullInfo(msg.groupId));

			int memberCount = group.memberCount;

			LinkedList<Integer> deletedAccounts = new LinkedList<>();

			for (int index = 0;index < memberCount;index += 200) {

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

				send(status.editText(text("没有找到 DA ...")));

				return;
				

			} else {

				send(status.editText(text("发现 " + deletedAccounts.size() + " 个 DA, 正在爆破 ...")));

			}

			long start = System.currentTimeMillis();

			for (Integer deleted : deletedAccounts) {

				send(new SetChatMemberStatus(msg.chatId,deleted,new ChatMemberStatusLeft()));
				
			}

			send(chatId(msg.chatId).input(inputText(text("清理完成 : 耗时 " + ((System.currentTimeMillis() - start) / 1000) + "s"))));

		}

	}

}
