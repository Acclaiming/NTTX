package io.kurumi.ntt.fragment.bots;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import io.kurumi.ntt.db.PointStore;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.abs.Callback;
import io.kurumi.ntt.fragment.abs.Function;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.fragment.abs.request.AbstractSend;
import io.kurumi.ntt.fragment.abs.request.ButtonLine;
import io.kurumi.ntt.fragment.abs.request.ButtonMarkup;
import io.kurumi.ntt.fragment.forum.ForumE;
import java.util.LinkedList;

public class MyBots extends Function {

	@Override
	public void functions(LinkedList<String> names) {

		names.add("mybots");

	}

	final String POINT_CHOOSE_BOT = "bot.c";
	final String POINT_BACK_TO_LIST = "bot.b";
	final String POINT_DELETE_BOT = "bot.d";


	@Override
	public void points(LinkedList<String> points) {

		points.add(POINT_CHOOSE_BOT);
		points.add(POINT_BACK_TO_LIST);
		points.add(POINT_DELETE_BOT);
		points.add(POINT_CONFIRM_DEL);
		
		points.add(POINT_CHAT_BOT_EDIT_MESSAGE);

		points.add(POINT_JOIN_SET_LOGCHANNEL);
		points.add(POINT_JOIN_SWITCH_DELJOIN);
		points.add(POINT_JOIN_DEL_LOGCHANNEL);
		
	}

	@Override
	public int target() {

		return Private;

	}
	
	@Override
	public void onFunction(final UserData user,Msg msg,String function,String[] params) {

		showBotList(user,msg,false);

	}

	@Override
	public void onCallback(UserData user,Callback callback,String point,String[] params) {

		if (POINT_CHOOSE_BOT.equals(point)) {

			long botId = Long.parseLong(params[0]);

			showBot(true,user,callback,botId);

		} else if (POINT_BACK_TO_LIST.equals(point)) {

			showBotList(user,callback,true);

			callback.confirm();

		} else if (POINT_DELETE_BOT.equals(point)) {

			long botId = Long.parseLong(params[0]);

			deleteBot(user,callback,botId);

		} else if (POINT_CONFIRM_DEL.equals(point)) {

			long botId = Long.parseLong(params[0]);

			confirmDelete(user,callback,botId);

		} else if (POINT_CHAT_BOT_EDIT_MESSAGE.equals(point)) {

			long botId = Long.parseLong(params[0]);

			chatBotEditMessage(user,callback,botId);

		} else if (POINT_JOIN_SWITCH_DELJOIN.equals(point)) {
			
			long botId = Long.parseLong(params[0]);

			joinBotSwitchDelJoin(user,callback,botId);
			
		} else if (POINT_JOIN_SET_LOGCHANNEL.equals(point)) {
			
			long botId = Long.parseLong(params[0]);

			editJoinBotLogChannel(user,callback,botId);
		
		} else if (POINT_JOIN_DEL_LOGCHANNEL.equals(point)) {
			
			long botId = Long.parseLong(params[0]);

			joinBotSwitchDelJoin(user,callback,botId);
			
		}

	}

	@Override
	public void onPoint(UserData user,Msg msg,PointStore.Point point) {

		if (point.point.equals(POINT_CHAT_BOT_EDIT_MESSAGE)) {

			editChatBotMessage(user,msg,(BotEdit)point.data);

		} else if (point.point.equals(POINT_JOIN_SET_LOGCHANNEL)) {
			
			joinBotLogChannelEdit(user,msg,(BotEdit)point.data);
			
		}

	}



	void showBotList(final UserData user,Msg msg,boolean edit) {

		if (UserBot.data.countByField("user",user.id) == 0) {

			msg.sendOrEdit(edit,"你还没有任何BOT ，使用 /newbot 创建一只新BOT ~").exec();

			return;

		}

		msg.sendOrEdit(edit,"从下方按钮中选择你的BOT :")
			.buttons(new ButtonMarkup() {{

					ButtonLine line = null;

					for (UserBot bot : UserBot.data.findByField("user",user.id)) {

						if (line == null) {

							line = newButtonLine();
							line.newButton("@" + bot.userName,POINT_CHOOSE_BOT,bot.id);

						} else {

							line.newButton("@" + bot.userName,POINT_CHOOSE_BOT,bot.id);
							line = null;

						}

					}

				}}).exec();

	}

	void showBot(boolean edit,UserData user,Msg msg,long botId) {

		final UserBot bot = UserBot.data.getById(botId);

		if (bot == null || !bot.user.equals(user.id)) {

			if (msg instanceof Callback) {

				((Callback)msg).alert("这个BOT无效");

			} else {

				msg.send("这个BOT无效...").exec();

			}

			showBotList(user,msg,true);
			
			return;

		}

		AbstractSend send = msg.sendOrEdit(edit,"自定义" + bot.typeName() + " : @" + bot.userName,"",bot.information());
		send.buttons(new ButtonMarkup() {{

					if (bot.type == 0) {

						newButtonLine("更改欢迎语",POINT_CHAT_BOT_EDIT_MESSAGE,bot.id);

					} else if (bot.type == 1) {
						
						Boolean delJoin = (Boolean) bot.params.get("delJoin");
						
						if (delJoin == null) delJoin = false;
						
						newButtonLine((delJoin ? "关闭" : "开启") + " 自动删除加群退群消息",POINT_JOIN_SWITCH_DELJOIN,bot.id);
						
						Long logChannel = (Long) bot.params.get("logChannel");
						
						newButtonLine((logChannel == null) ? "设置日志频道" : "修改日志频道",POINT_JOIN_SET_LOGCHANNEL,bot.id);
						
						if (logChannel != null) {
							
							newButtonLine("删除日志频道",POINT_JOIN_DEL_LOGCHANNEL,bot.id);
							
						}
						
					}

					newButtonLine()
						.newButton("删除BOT",POINT_DELETE_BOT,bot.id)
						.newButton("返回列表",POINT_BACK_TO_LIST);

				}}).exec();

	}

	final String POINT_CONFIRM_DEL = "bot.d.c";

	void deleteBot(UserData user,Callback callback,long botId) {

		final UserBot bot = UserBot.data.getById(botId);

		if (bot == null || !bot.user.equals(user.id)) {

			callback.alert("这个BOT无效");

			showBotList(user,callback,true);

			return;

		}

		callback
			.edit("确认要删除 @" + bot.userName + " 吗？你会失去这只BOT，真的很久")
			.buttons(new ButtonMarkup() {{

					newButtonLine("不删了",POINT_CHOOSE_BOT,bot.id);
					newButtonLine("手滑了",POINT_CHOOSE_BOT,bot.id);
					newButtonLine("点着玩",POINT_CHOOSE_BOT,bot.id);
					newButtonLine("删掉罢",POINT_CONFIRM_DEL,bot.id);

				}}).exec();

	}

	void confirmDelete(UserData user,Callback callback,long botId) {

		final UserBot bot = UserBot.data.getById(botId);

		if (bot == null || !bot.user.equals(user.id)) {

			callback.alert("这个BOT无效");

			showBotList(user,callback,true);

			return;

		}

		bot.stopBot();
		
		UserBot.data.deleteById(bot.id);
		
		showBotList(user,callback,true);

		callback.alert("已删除 @" + bot.userName);

	}

	final String POINT_CHAT_BOT_EDIT_MESSAGE = "bot.c.e";
	
	final String POINT_JOIN_SWITCH_DELJOIN = "bot.j.j";
	final String POINT_JOIN_SET_LOGCHANNEL = "bot.j.s";
	final String POINT_JOIN_DEL_LOGCHANNEL = "bot.j.d";
	
	static class BotEdit {

		LinkedList<Msg> msg = new LinkedList<>();
		
		UserBot bot;

	}

	void chatBotEditMessage(UserData user,Callback callback,long botId) {

		final UserBot bot = UserBot.data.getById(botId);

		if (bot == null || !bot.user.equals(user.id)) {

			callback.alert("这个BOT无效");

			showBotList(user,callback,true);

			return;

		}

		callback.confirm();

		callback.edit("好,现在发送新的欢迎语 :").withCancel().exec();

		BotEdit point = new BotEdit();

		point.bot = bot;
		point.msg.add(callback);

		setPoint(user,POINT_CHAT_BOT_EDIT_MESSAGE,point);

	}

	void editChatBotMessage(UserData user,Msg msg,BotEdit data) {
		
		data.msg.add(msg);
		
		if (!msg.hasText()) {

			data.msg.add(msg.send("你正在设置 @" + data.bot.userName + " 的欢迎语 ，请输入 : ").withCancel().send());

			return;

		}

		clearPoint(user);
		
		data.bot.params.put("msg",msg.text());

		UserBot.data.setById(data.bot.id,data.bot);
		
		data.bot.reloadBot();
		
		for (Msg it : data.msg) it.delete();
		
		msg.send("修改成功！").exec();

		showBot(false,user,msg,data.bot.id);

	}
	
	void joinBotSwitchDelJoin(UserData user,Callback callback,long botId) {

		final UserBot bot = UserBot.data.getById(botId);

		if (bot == null || !bot.user.equals(user.id)) {

			callback.alert("这个BOT无效");

			showBotList(user,callback,true);

			return;

		}

		Boolean delJoin = (Boolean) bot.params.get("delJoin");
		
		if (delJoin == null) delJoin = true; else delJoin = !delJoin;
		
		bot.params.put("delJoin",delJoin);
		
		callback.text((delJoin ? "开启" : "关闭") + "成功 ~");
		
		UserBot.data.setById(bot.id,bot);

		bot.reloadBot();

		showBot(true,user,callback,bot.id);
		
	}
	
	void editJoinBotLogChannel(UserData user,Callback callback,long botId) {

		final UserBot bot = UserBot.data.getById(botId);

		if (bot == null || !bot.user.equals(user.id)) {

			callback.alert("这个BOT无效");

			showBotList(user,callback,true);

			return;

		}

		callback.confirm();

		callback.edit("好,现在直接转发一条这个频道的消息 (如果没有，就发送一条) :").withCancel().exec();

		BotEdit point = new BotEdit();

		point.bot = bot;
		point.msg.add(callback);

		setPoint(user,POINT_CHAT_BOT_EDIT_MESSAGE,point);

	}

	void joinBotLogChannelEdit(UserData user,Msg msg,BotEdit data) {

		Message message = msg.message();

		Chat chat = message.forwardFromChat();

		if (chat == null || chat.type() != Chat.Type.channel) {

			msg.send("请直接转发一条频道消息 : 如果没有消息，那就自己发一条").withCancel().exec();

			return;

		}

		TelegramBot bot = new TelegramBot(data.bot.token);

		SendResponse resp = bot.execute(new SendMessage(chat.id(),"Test").disableNotification(true));

		if (!resp.isOk()) {

			msg.send("设置的BOT无法在该频道 (" + chat.title() + ") 发言... 请重试").withCancel().exec();

			return;

		}

		clearPoint(user);
		
		data.bot.params.put("logChannel",chat.id());

		UserBot.data.setById(data.bot.id,data.bot);

		data.bot.reloadBot();
		
		for (Msg it : data.msg) it.delete();

		msg.send("修改成功！").exec();

		showBot(false,user,msg,data.bot.id);

	}
	
	void joinBotDelLogChannel(UserData user,Callback callback,long botId) {

		final UserBot bot = UserBot.data.getById(botId);

		if (bot == null || !bot.user.equals(user.id)) {

			callback.alert("这个BOT无效");

			showBotList(user,callback,true);

			return;

		}

		bot.params.remove("logChannel");

		callback.text("已移除");

		UserBot.data.setById(bot.id,bot);

		bot.reloadBot();

		showBot(false,user,callback,bot.id);

	}
	

}
