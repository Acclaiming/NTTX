package io.kurumi.ntt.fragment.bots;

import cn.hutool.core.util.ArrayUtil;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.model.Msg;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import cn.hutool.core.util.NumberUtil;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.LeaveChat;
import com.pengrad.telegrambot.request.KickChatMember;

import java.util.List;
import java.util.*;

public class UserBotFragment extends BotFragment {

    private UserBot bot;

    public Long botId;

    public static HashMap<Long, UserBotFragment> bots = new HashMap<>();

    private String userName;
    private String botToken;

    public Long userId;

    public Map<String, Object> params;
    public Set<Long> banned_chat;

    @Override
    public void reload() {

        super.reload();

		// addFragment(new BotChannnel());

        bot = UserBot.data.getById(botId);

        botId = bot.id;

        bots.put(botId, this);

        userName = bot.userName;
        botToken = bot.token;
        userId = bot.user;

        params = bot.params;

    }

	/*

	 List<Long> banned_chat_list = getParam("banned_chat");

	 if (banned_chat_list != null) {

	 banned_chat = new HashSet<Long>(banned_chat_list);

	 }

	 if (banned_chat == null) {

	 banned_chat = new HashSet<>();

	 params.put("banned_chat",banned_chat);

	 }

	 localAdmins.clear();
	 localAdmins.add(userId);

	 }

	 @Override
	 public void init(BotFragment origin) {

	 super.init(origin);

	 registerFunction("start");

	 }

	 */

    public UserData getOwner() {

        return UserData.get(userId);

    }

    @Override
    public String botName() {

        return getClass().getSimpleName();

    }

    @Override
    public String getToken() {

        return botToken;

    }

    public <T> T getParam(String key) {

        return (T) params.get(key);

    }

    public void setParam(String key, Object value) {

        params.put(key, value);


    }

    public void save() {

		/*

		 if (banned_chat.isEmpty()) {

		 params.remove("banned_chat");

		 }

		 */

        UserBot.data.setById(botId, bot);

    }

	/*

	 @Override
	 public void onFunction(UserData user,Msg msg,String function,String[] params) {

	 super.onFunction(user,msg,function,params);

	 if (msg.isPrivate() && (userId.equals(user.id) || user.admin())) {

	 if ("start".equals(function)) {

	 msg.send(
	 "管理员命令 :\n",
	 "/send <chatId> <text...>",
	 "/edit <chatId> <messageId> <text...>",
	 "/delete_message <chatId> <messageId>",
	 "/forward <toChatId> <fromChatId> <messageId>",
	 "/export_link <chatid>",
	 "/restrict <chatId> <userId>",
	 "/promote <chatId> <userId>",
	 "/kick <chatId> <userId>",
	 "/unban <chatId> <userId>",
	 "/pin <chatId> <messageId>",
	 "/unpin <chatId>",
	 "/exit <chatId>",
	 "/get_file <fileId>",
	 "/send_file <chatId> <fileId>",
	 "/get_admins <chatId>",
	 "/get_members_count <chatId>",
	 "/get_member <chatId> <userId>",
	 "/ban_chat <chatId>",
	 "/unban_chat <chatId>").exec();

	 } else if ("ban_chat".equals(function)) {

	 if (params.length < 1) { invalidParams(msg,"chatId"); return; }

	 long target = NumberUtil.parseLong(params[0]);

	 if (userId.equals(target)) {

	 msg.send("不能屏蔽你自己...").exec();

	 return;

	 }

	 if (banned_chat.add(target)) {

	 msg.send("已屏蔽 如果未退出 请手动退出").exec();

	 } else {

	 msg.send("已经屏蔽过了...").exec();

	 }

	 } else if ("unban_chat".equals(function)) {

	 if (params.length < 1) { invalidParams(msg,"chatId"); return; }

	 if (banned_chat.remove((NumberUtil.parseLong(params[0])))) {

	 msg.send("已解除").exec();

	 } else {

	 msg.send("没有屏蔽过...").exec();

	 }

	 }

	 return;



	 }

	 }

	 @Override
	 public int checkMsg(UserData user,Msg msg) {

	 if (msg.isPrivate() && !(user.admin() || user.id.equals(userId)) && banned_chat.contains(msg.chatId())) {

	 return PROCESS_REJECT;

	 }

	 if (msg.message().newChatMembers() != null) {

	 User newMember = msg.message().newChatMembers()[0];

	 if (newMember.id().equals(botId) && banned_chat.contains(msg.chatId()))  {

	 execute(new LeaveChat(msg.chatId()));

	 return PROCESS_REJECT;

	 } else if (banned_chat.contains(newMember.id())) {

	 execute(new KickChatMember(msg.chatId(),newMember.id().intValue()));

	 return PROCESS_REJECT;

	 }

	 } else if (msg.message().leftChatMember() != null) {

	 if (banned_chat.contains(msg.message().leftChatMember().id())) {

	 msg.delete();

	 return PROCESS_REJECT;

	 }

	 }

	 return PROCESS_ASYNC;

	 }

	 */

    void invalidParams(Msg msg, String... params) {

        msg.send("无效的参数 , /" + msg.command() + " <" + ArrayUtil.join(params, "> <") + ">").exec();

    }

    @Override
    public void stop() {

        save();

        super.stop();

    }

}
