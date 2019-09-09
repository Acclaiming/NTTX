package io.kurumi.ntt.listeners.group;

import io.kurumi.ntt.td.TdApi.*;
import io.kurumi.ntt.td.client.TdException;
import io.kurumi.ntt.td.client.TdListener;
import io.kurumi.ntt.td.model.TMsg;

import java.util.LinkedList;

public class CleanAccounts extends TdListener {

    @Override
    public void init() {

        registerFunction("clean_da", "clean_all");

    }

    @Override
    public boolean asyncFunction() {

        return true;

    }

    @Override
    public void onFunction(User user, TMsg msg, String function, String[] params) {

        if (msg.isPrivate()) {

            sendHTML(msg, getLocale(user).CA_HELP);

            return;

        }

        boolean cleanAll = function.endsWith("all");

        if (!msg.isChannel()) {

            ChatMember chatMember = execute(new GetChatMember(msg.chatId, user.id));

            if (!(chatMember.status instanceof ChatMemberStatusCreator || chatMember.status instanceof ChatMemberStatusAdministrator)) {

                sendText(msg, getLocale(user).NOT_CHAT_ADMIN);

                return;

            }

        }

        int size;

        LinkedList<Integer> targetAccounts = new LinkedList<>();

        if (msg.isBasicGroup()) {

            BasicGroupFullInfo info = execute(new GetBasicGroupFullInfo(msg.groupId));

            size = info.members.length;

            for (ChatMember memberId : info.members) {

                if (memberId.status instanceof ChatMemberStatusCreator || memberId.status instanceof ChatMemberStatusAdministrator || memberId.userId == client.me.id)
                    continue;

                if (cleanAll) {

                    targetAccounts.add(memberId.userId);

                } else {

                    User member = execute(new GetUser(memberId.userId));

                    if (member.type instanceof UserTypeDeleted) {

                        targetAccounts.add(member.id);

                    }

                }

            }

        } else {

            SupergroupFullInfo group = execute(new GetSupergroupFullInfo(msg.groupId));

            size = group.memberCount;

            for (int index = 0; index < size; index += 200) {

                ChatMembers members = null;

                try {

                    members = execute(new GetSupergroupMembers(msg.groupId, new SupergroupMembersFilterRecent(), index, 200));

                } catch (TdException e) {

                    try {

                        members = execute(new GetSupergroupMembers(msg.groupId, new SupergroupMembersFilterRecent(), index, 200));

                        // TDLib 第一次取 会出错

                    } catch (TdException ex) {
                    }

                }

                for (ChatMember memberId : members.members) {

                    if (memberId.status instanceof ChatMemberStatusCreator || memberId.status instanceof ChatMemberStatusAdministrator || memberId.userId == client.me.id)
                        continue;

                    if (cleanAll) {

                        targetAccounts.add(memberId.userId);

                    } else {

                        User member = execute(new GetUser(memberId.userId));

                        if (member.type instanceof UserTypeDeleted) {

                            targetAccounts.add(member.id);

                        }

                    }

                }

            }

            if (targetAccounts.isEmpty()) {

                sendText(msg, getLocale(user).CA_NOT_FOUND);

                return;

            } else {

                sendText(msg, getLocale(user).CA_FOUND, targetAccounts.size());

            }

            long start = System.currentTimeMillis();

            for (Integer deleted : targetAccounts) {

                send(new SetChatMemberStatus(msg.chatId, deleted, new ChatMemberStatusLeft()));

            }

            sendText(msg, getLocale(user).CA_FINISH, (System.currentTimeMillis() - start) / 1000);

        }

    }

}
