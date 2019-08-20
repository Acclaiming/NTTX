package io.kurumi.ntt.fragment.group.options;

import cn.hutool.core.util.NumberUtil;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.ChatMember;
import com.pengrad.telegrambot.request.GetChatMember;
import com.pengrad.telegrambot.response.GetChatMemberResponse;
import io.kurumi.ntt.db.GroupData;
import io.kurumi.ntt.db.PointData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.ButtonMarkup;

public class LogMain extends Fragment {

	public static String POINT_LOG = "group_log";

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerCallback(POINT_LOG);
		registerPoint(POINT_LOG);

	}

	@Override
	public void onCallback(UserData user,Callback callback,String point,String[] params) {

		if (params.length == 0 || !NumberUtil.isNumber(params[0])) {

			callback.invalidQuery();

			return;

		}

        final GroupData data = GroupData.data.getById(NumberUtil.parseLong(params[0]));

		if (data == null) {

			callback.invalidQuery();

			return;

		}

		if (params.length == 1) {

			callback.edit(logStat(data)).buttons(logMenu(data)).html().async();

			return;

		}

		if ("switch".equals(params[1])) {

			if (data.enable_log == null) {

				if (data.log_channel == null) {

					callback.alert("你还没有设置日志频道 无法开启 :)");

					return;

				}

				data.enable_log = true;

				callback.text("🎥  已开启");

			} else {

				data.enable_log = null;

				callback.text("🎥  已关闭");

			}
			
			callback.edit(logStat(data)).buttons(logMenu(data)).async();

		} else if ("set_channel".equals(params[1])) {

			callback.confirm();

			EditCustom edit = new EditCustom(callback, data);

			callback.send("现在转发一条该频道的消息 没有就发一条 :)").exec(edit);

			setPrivatePoint(user, POINT_LOG, edit);

		}

		
	}
	
	@Override
    public void onPoint(UserData user, Msg msg, String point, PointData data) {

        EditCustom edit = (EditCustom) data.with(msg);
		
		if (msg.message().forwardFromChat() == null) {

			msg.send("请转发一条将要被设为日志频道的频道的消息").withCancel().exec(data);

			return;

		} else if (msg.message().forwardFromChat().type() != Chat.Type.channel) {

			msg.send("这条消息不来自一个频道 ！").withCancel().exec(data);

			return;

		}

		long channelId = msg.message().forwardFromChat().id();

		GetChatMemberResponse resp = execute(new GetChatMember(channelId,user.id.intValue()));

		if (resp == null) {

			msg.send("Telegram 超时 请重试").exec(data);

			return;

		} else if (!resp.isOk()) {

			msg.send("BOT不在该频道 ( " + resp.errorCode() + " : " + resp.description() + " )").exec(data);

			return;

		} else if (!(resp.chatMember().status() == ChatMember.Status.creator || resp.chatMember().status() == ChatMember.Status.administrator)) {

			msg.send("你不是该频道的管理员 :)").exec(data);

			return;

		}

		edit.data.log_channel = channelId;

		clearPrivatePoint(user);

    }

  

	String logStat(final GroupData data) {

		String message = "设置机器人群组管理操作日志选项. ";
		
		message += "\n\n日志频道 : ";

		if (data.log_channel == null) {

			message += "未设定";

		} else {

			message += data.log_channel;

		}
		
		message += "\n\n" + OptionsMain.doc;

		return message;

	}

	final class EditCustom extends PointData {

        Callback origin;
        GroupData data;

        public EditCustom(Callback origin,GroupData data) {
			
            this.origin = origin;
            this.data = data;

        }

        @Override
        public void onFinish() {

			super.onFinish();

			origin.edit(logStat(data)).buttons(logMenu(data)).async();
            
        }

    }

	ButtonMarkup logMenu(final GroupData data) {

        return new ButtonMarkup() {{

				newButtonLine()
                    .newButton("开启日志")
                    .newButton(data.enable_log != null ? "✅" : "☑",POINT_LOG,data.id,"switch");

				newButtonLine("设置日志频道",POINT_LOG,data.id,"set_channel");

				newButtonLine("🔙",OptionsMain.POINT_OPTIONS,data.id);

			}};

	}

}

