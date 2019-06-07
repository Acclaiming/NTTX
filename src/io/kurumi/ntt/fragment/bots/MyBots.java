package io.kurumi.ntt.fragment.bots;

import io.kurumi.ntt.db.PointStore;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.funcs.abs.Function;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.ButtonLine;
import io.kurumi.ntt.model.request.ButtonMarkup;
import io.kurumi.ntt.model.request.Edit;
import java.util.LinkedList;
import io.kurumi.ntt.fragment.bots.MyBots.ChatBotEditMessage;
import io.kurumi.ntt.model.request.AbstractSend;

public class MyBots extends Function {

	@Override
	public void functions(LinkedList<String> names) {

		names.add("mybots");

	}

	final String POINT_CHOOSE_BOT = "bot.c";
	final String POINT_BACK_TO_LIST = "bot.b";
	final String POINT_DELETE_BOT = "bot.d";

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

		}

	}

	@Override
	public void onPoint(UserData user,Msg msg,PointStore.Point point) {

		if (point.point.equals(POINT_CHAT_BOT_EDIT_MESSAGE)) {

			editChatBotMessage(user,msg,(ChatBotEditMessage)point.data);

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

					}

					newButtonLine()
						.newButton("删除BOT",POINT_DELETE_BOT,bot.id)
						.newButton("返回列表",POINT_BACK_TO_LIST);

				}});

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
					newButtonLine("删掉罢",POINT_DELETE_BOT,bot.id);

				}}).exec();

	}

	void confirmDelete(UserData user,Callback callback,long botId) {

		final UserBot bot = UserBot.data.getById(botId);

		if (bot == null || !bot.user.equals(user.id)) {

			callback.alert("这个BOT无效");

			showBotList(user,callback,true);

			return;

		}

		UserBot.data.deleteById(bot.id);

		showBotList(user,callback,true);

		callback.alert("已删除 @" + bot.userName);

	}

	final String POINT_CHAT_BOT_EDIT_MESSAGE = "bot.c.e";

	static class ChatBotEditMessage {

		Msg message;
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

		callback.edit("好,现在发送新的欢迎语 :","或使用 /cancel 取消").exec();

		ChatBotEditMessage point = new ChatBotEditMessage();

		point.bot = bot;
		point.message = callback;

		setPoint(user,POINT_CHAT_BOT_EDIT_MESSAGE,point);

	}

	void editChatBotMessage(UserData user,Msg msg,ChatBotEditMessage data) {

		if (!msg.hasText()) {

			msg.send("你正在设置 @" + data.bot.userName + " 的欢迎语 ，请输入 : ","或使用 /cancel 取消").exec();

			return;

		}

		data.bot.params.put("msg",msg.text());

		UserBot.data.setById(data.bot.id,data.bot);
		
		data.message.delete();

		msg.delete();

		msg.send("修改成功！").exec();

		showBot(false,user,msg,data.bot.id);

	}

}
