package io.kurumi.ntt.fragment.forum.admin;

import cn.hutool.core.util.NumberUtil;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.GetMe;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetMeResponse;
import com.pengrad.telegrambot.response.SendResponse;
import io.kurumi.ntt.db.PointStore;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.forum.ForumE;
import io.kurumi.ntt.fragment.forum.ForumPost;
import io.kurumi.ntt.funcs.abs.Function;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.ButtonMarkup;
import io.kurumi.ntt.model.request.Keyboard;
import io.kurumi.ntt.utils.MongoIDs;
import java.util.LinkedList;
import java.util.List;

public class ForumManage extends Function {

	@Override
	public void functions(LinkedList<String> names) {

		names.add("forum");

	}

	@Override
	public int target() {

		return Private;

	}

	final String POINT_CREATE_FORUM = "forum.create";
	final String POINT_FORUM_MANAGE = "forum.main";
	final String POINT_EDIT_NAME = "forum.name";
	final String POINT_EDIT_DESC = "forum.desc";
	final String POINT_EDIT_TOKEN = "forum.token";
	final String POINT_EDIT_CHAN = "forum.chan";

	final String POINT_EDIT_ADMIN = "forum.admin";

	@Override
	public void points(LinkedList<String> points) {

		points.add(POINT_CREATE_FORUM);
		points.add(POINT_FORUM_MANAGE);
		
		points.add(POINT_EDIT_NAME);
		points.add(POINT_EDIT_DESC);
		points.add(POINT_EDIT_CHAN);
		points.add(POINT_EDIT_TOKEN);
		points.add(POINT_EDIT_ADMIN);

	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		long count = ForumE.data.countByField("owner",user.id);

		if (count == 0 && params.length > 0 && "init".equals(params[0])) {

			createForum(user,msg);

			return;

		} else if (count == 0) {

			msg.send("没有创建论坛，使用 /forum init 来创建。").exec();

			return;

		}

		forumMain(false,user,msg);

	}

	class ForumCreate {

		int progress = 0;

		// ---------------

		String token;

		TelegramBot bot;

		User botMe;

		// ---------------

		long channelId;

	}

	void createForum(UserData user,Msg msg) {

		String[] desc = new String[] {

			"电报论坛是一个由 NTT 驱动的基于 Channel 和 Bot 的简中论坛程序。\n",

			"要创建论坛，你必须同意 : 喵...\n",

			"警告 : 目前程序并未稳定，随时可能需要清除数据以更新程序",

		};

		setPoint(user,POINT_CREATE_FORUM);

		msg.send(desc).keyboard(new Keyboard() {{

					newButtonLine().newButton("同意").newButton("拒绝");

				}}).exec();

	}

	String cancel = "\n使用 /cancel 取消创建 :)";

	@Override
	public void onPoint(UserData user,Msg msg,PointStore.Point point) {

		switch (point.point) {

			case POINT_CREATE_FORUM : createForum(user,msg,point);break;
			case POINT_EDIT_NAME : forumNameEdit(user,msg,point);break;
			case POINT_EDIT_DESC : forumDiscEdit(user,msg,point);break;

		}

	}

	void createForum(UserData user,Msg msg,PointStore.Point point) {

		if (point.data == null) {

			if ("同意".equals(msg.text())) {

				msg.send("这足够公平,如果你考虑好了就再来。").removeKeyboard().exec();

				clearPoint(user);

				return;

			}

			point.data = new ForumCreate();

			msg.send("好，现在输入用于论坛的BotToken :","\nBotToken可以当成TelegramBot登录的账号密码、需要在 @BotFather 申请。",cancel).removeKeyboard().exec();

			return;

		}

		ForumCreate create = (ForumCreate) point.data;

		if (create.progress == 0) {

			if (!msg.hasText() ||  !msg.text().contains(":")) {

				msg.send("无效的Token.请重试. ","Token 看起来像这样: '12345678:ABCDEfgHIDUROVjkLmNOPQRSTUvw-cdEfgHI'",cancel).exec();

				return;

			}

			msg.send("正在检查BOT信息...").exec();

			GetMeResponse me = (create.bot = new TelegramBot(create.token = msg.text())).execute(new GetMe());

			if (!me.isOk()) {

				msg.send("Token无效... 请重新输入",cancel).exec();

				return;

			}

			create.botMe = me.user();

			create.progress = 1;

			String[] channel = new String[] {

				"现在发送作为论坛版面的频道 (Channel) :\n",

				"你使用的 @" + me.user().username() + " 必须可以在频道发言",
				"现在转发一条频道的消息来,以设置频道\n",

				"不用担心，频道这可以在创建造成后更改 :)",

				cancel

			};

			msg.send(channel).exec();

		} else if (create.progress == 1) {

			Message message = msg.message();

			Chat chat = message.forwardFromChat();

			if (chat == null || chat.type() != Chat.Type.channel) {

				msg.send("请直接转发一条频道消息 : 如果没有消息，那就自己发一条",cancel).exec();

				return;

			}

			SendResponse resp = create.bot.execute(new SendMessage(chat.id(),"TestSendable").disableNotification(true));

			if (!resp.isOk()) {

				msg.send("设置的BOT @" + create.botMe.username() + " 无法在该频道 (" + chat.title() + ") 发言... 请重试",cancel).exec();

				return;

			}

			create.channelId = chat.id();

			create.progress = 2;

			msg.send("十分顺利。现在发送论坛的名称 : 十个字以内 ","\n如果超过、你可以手动设置频道和BOT的名称 (如果字数允许) ,这里的名称仅作为一个简称",cancel).exec();

		} else if (create.progress == 2) {

			if (!msg.hasText()) {

				msg.send("忘记了吗？你正在创建一个论坛。现在发送名称 :").exec();

				return;

			}

			if (msg.text().length() > 10) {

				msg.send("好吧，再说一遍。名称限制十个字 : 你可以手动设置频道和BOT的名称 (如果字数允许) ,这里的名称仅作为一个简称",cancel).exec();

				return;

			}

			ForumE forum = new ForumE();

			forum.name = msg.text();
			forum.owner = user.id;
			forum.token = create.token;
			forum.channel = create.channelId;

			forum.id = MongoIDs.getNextId(ForumE.class.getSimpleName());

			ForumE.data.setById(forum.id,forum);

		}

	}

	class ForumEdit {

		long forumId;
		List<Msg> msg = new LinkedList<>();

	}

	void forumMain(boolean edit,UserData user,Msg msg) {

		final ForumE forum = ForumE.data.getByField("owner",user.id);

		if (forum == null) {

			msg.sendOrEdit(edit,"没有创建论坛，使用 /forum init 来创建。").exec();

			return;

		}

		String[] info = new String[] {

			"论坛名称 : " + forum.name,
			"论坛简介 : " + forum.description,

			"\n绑定频道 : " + forum.channel,
			"BotToken : " + forum.token.substring(0,11) + "...",
			"状态 : " + forum.error == null ? "正常运行" : forum.error,

			"\n帖子数量 : " + ForumPost.data.countByField("forumId",forum.id),

		};

		ButtonMarkup buttons = new ButtonMarkup() {{

				newButtonLine()
					.newButton("修改名称",POINT_EDIT_NAME,forum.id)
					.newButton("修改简介",POINT_EDIT_DESC,forum.id);

				newButtonLine()
					.newButton("修改频道",POINT_EDIT_CHAN,forum.id)
					.newButton("修改Token",POINT_EDIT_TOKEN,forum.id);

				newButtonLine()
					.newButton("管理员设置",POINT_EDIT_ADMIN,forum.id);

				// TODO : 删除

			}};

		msg.sendOrEdit(edit,info).buttons(buttons).exec();

	}

	@Override
	public void onCallback(UserData user,Callback callback,String point,String[] params) {

		long forumId = NumberUtil.parseLong(params[0]);

		switch (point) {

			case POINT_EDIT_NAME : editForumName(user,callback,forumId);break;
			case POINT_EDIT_DESC :editForumDesc(user,callback,forumId);break;

		}

	}

	void editForumName(UserData user,Callback callback,long forumId) {

		ForumEdit edit = new ForumEdit();

		edit.forumId = forumId;

		setPoint(user,POINT_EDIT_NAME,edit);

		callback.confirm();

		edit.msg.add(callback.send("好。现在发送新的论坛名称 : 十个字以内 ","\n如果超过、你可以手动设置频道和BOT的名称 (如果字数允许) ,这里的名称仅作为一个简称",cancel).send());

	}

	void forumNameEdit(UserData user,Msg msg,PointStore.Point point) {

		ForumEdit edit = (ForumEdit)point.data;

		long forumId = edit.forumId;

		if (!msg.hasText()) {

			edit.msg.add(msg);
			edit.msg.add(msg.send("忘记了吗？你正在修改论坛名称。现在发送新名称 :").send());

			return;

		}

		if (msg.text().length() > 10) {

			edit.msg.add(msg);
			edit.msg.add(msg.send("好吧，再说一遍。名称限制十个字 : 你可以手动设置频道和BOT的名称 (如果字数允许) ,这里的名称仅作为一个简称",cancel).send());

			return;

		}

		clearPoint(user);

		ForumE forum = ForumE.data.getById(forumId);

		forum.name = msg.text();

		ForumE.data.setById(forumId,forum);

		for (Msg it : edit.msg) it.delete();

		msg.send("修改成功 : 请使用 '重置频道信息' 来立即更新缓存").exec();

		forumMain(false,user,msg);

	}

	void editForumDesc(UserData user,Callback callback,long forumId) {

		ForumEdit edit = new ForumEdit();

		edit.forumId = forumId;

		setPoint(user,POINT_EDIT_DESC,edit);

		callback.confirm();

		edit.msg.add(callback.send("好。现在发送新的论坛简介 : 160字以内 , 你可以开一个置顶帖详细介绍论坛或是规定。",cancel).send());

	}

	void forumDiscEdit(UserData user,Msg msg,PointStore.Point point) {

		ForumEdit edit = (ForumEdit)point.data;

		long forumId = edit.forumId;

		if (!msg.hasText()) {

			edit.msg.add(msg);
			edit.msg.add(msg.send("忘记了吗？你正在修改论坛简介。现在发送新简介 :").send());

			return;

		}

		if (msg.text().length() > 160) {

			edit.msg.add(msg);
			edit.msg.add(msg.send("好吧，再说一遍。简介限制160字 : 你可以开一个置顶帖详细介绍论坛或是规定 。",cancel).send());

			return;

		}

		clearPoint(user);

		ForumE forum = ForumE.data.getById(forumId);

		forum.description = msg.text();

		ForumE.data.setById(forumId,forum);

		for (Msg it : edit.msg) it.delete();

		msg.send("修改成功 : 请使用 '重置频道信息' 来立即更新缓存").exec();

		forumMain(false,user,msg);

	}

}
