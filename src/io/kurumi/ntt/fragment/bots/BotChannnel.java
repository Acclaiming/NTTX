package io.kurumi.ntt.fragment.bots;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.json.JSONObject;
import com.pengrad.telegrambot.request.DeleteChatPhoto;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.ExportChatInviteLink;
import com.pengrad.telegrambot.request.ForwardMessage;
import com.pengrad.telegrambot.request.GetChat;
import com.pengrad.telegrambot.request.GetChatAdministrators;
import com.pengrad.telegrambot.request.GetChatMember;
import com.pengrad.telegrambot.request.GetChatMembersCount;
import com.pengrad.telegrambot.request.GetMe;
import com.pengrad.telegrambot.request.KickChatMember;
import com.pengrad.telegrambot.request.LeaveChat;
import com.pengrad.telegrambot.request.PinChatMessage;
import com.pengrad.telegrambot.request.PromoteChatMember;
import com.pengrad.telegrambot.request.RestrictChatMember;
import com.pengrad.telegrambot.request.UnbanChatMember;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.StringResponse;
import io.kurumi.ntt.db.PointData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.Html;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.response.GetFileResponse;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendMessage;
import io.kurumi.ntt.model.request.Send;
import com.pengrad.telegrambot.request.UnpinChatMessage;
import com.pengrad.telegrambot.response.SendResponse;
import io.kurumi.ntt.model.request.Edit;

public class BotChannnel extends Fragment {

	@Override
	public void init(BotFragment origin) {

		super.init(origin);
		
		registerAdminFunction("send","edit","delete","forward","export_link","restrict","promote","kick","pin","unpin","unban","exit","get_file","send_file","get_chat","get_admins","get_members_count","get_member","get_me");
		registerPoint(POINT_INPUT_EXTRA_PARAM);
		
	}

	final String POINT_INPUT_EXTRA_PARAM = "channel_input";

	class ExtraParam extends PointData {

		int type;
		int step = 0;

		public ExtraParam(int type,Object data) {

			this.type = type;
			this.data = data;

		}

	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		if ("delete_chat_photo".equals(function)) {

			if (params.length < 1) { invalidParams(msg,"chatId"); return; }

			sendResult(msg,execute(new DeleteChatPhoto(NumberUtil.parseLong(params[0]))));

		} else if ("delete".equals(function)) {

			if (params.length < 2) { invalidParams(msg,"chatId","messageId"); return; }

			sendResult(msg,execute(new DeleteMessage(NumberUtil.parseLong(params[0]),NumberUtil.parseInt(params[1]))));

		} else if ("export_link".equals(function)) {

			if (params.length < 1) { invalidParams(msg,"chatId"); return; }

			sendResult(msg,execute(new ExportChatInviteLink(NumberUtil.parseLong(params[0]))));

		} else if ("forward".equals(function)) {

			if (params.length < 3) { invalidParams(msg,"fromChatId","toChatId","messageId"); return; }

			ForwardMessage send = new ForwardMessage(NumberUtil.parseLong(params[0]),NumberUtil.parseLong(params[1]),NumberUtil.parseInt(params[2]));

			ExtraParam param = new ExtraParam(0,send);

			setPrivatePoint(user,POINT_INPUT_EXTRA_PARAM,param);

			msg.send("disable_notification ？").keyboard("True","False").exec();

		} else if ("get_chat".equals(function)) {

			if (params.length < 1) { invalidParams(msg,"chatId"); return; }

			sendResult(msg,execute(new GetChat(NumberUtil.parseLong(params[0]))));

		}  else if ("get_admins".equals(function)) {

			if (params.length < 1) { invalidParams(msg,"chatId"); return; }

			sendResult(msg,execute(new GetChatAdministrators(NumberUtil.parseLong(params[0]))));

		} else if ("get_member".equals(function)) {

			if (params.length < 2) { invalidParams(msg,"chatId","userId"); return; }

			sendResult(msg,execute(new GetChatMember(NumberUtil.parseLong(params[0]),NumberUtil.parseInt(params[1]))));

		}  else if ("get_members_count".equals(function)) {

			if (params.length < 1) { invalidParams(msg,"chatId"); return; }

			sendResult(msg,execute(new GetChatMembersCount(NumberUtil.parseLong(params[0]))));

		} else if ("get_me".equals(function)) {

			sendResult(msg,execute(new GetMe()));

		} else if ("kick".equals(function)) {

			if (params.length < 2) { invalidParams(msg,"chatId","userId"); return; }

			KickChatMember send = new KickChatMember(NumberUtil.parseLong(params[0]),NumberUtil.parseInt(params[1]));

			ExtraParam param = new ExtraParam(1,send);

			setPrivatePoint(user,POINT_INPUT_EXTRA_PARAM,param);

			msg.send("ban ？").keyboard("True","False").exec();

		} else if ("promote".equals(function)) {

			if (params.length < 2) { invalidParams(msg,"chatId","userId"); return; }

			KickChatMember send = new KickChatMember(NumberUtil.parseLong(params[0]),NumberUtil.parseInt(params[1]));

			ExtraParam param = new ExtraParam(2,send);

			setPrivatePoint(user,POINT_INPUT_EXTRA_PARAM,param);

			msg.send("UnPromote ？").keyboard("True","False").exec();

		} else if ("restrict".equals(function)) {

			if (params.length < 2) { invalidParams(msg,"chatId","userId"); return; }

			RestrictChatMember send = new RestrictChatMember(NumberUtil.parseLong(params[0]),NumberUtil.parseInt(params[1]));

			ExtraParam param = new ExtraParam(4,send);

			setPrivatePoint(user,POINT_INPUT_EXTRA_PARAM,param);

			msg.send("can_send_messages ？").keyboard("True","False","Read Only").exec();

		} else if ("exit".equals(function)) {

			if (params.length < 1) { invalidParams(msg,"chatId"); return; }

			sendResult(msg,execute(new LeaveChat(NumberUtil.parseLong(params[0]))));

		} else if ("pin".equals(function)) {

			if (params.length < 2) { invalidParams(msg,"chatId","messageId"); return; }

			PinChatMessage send = new PinChatMessage(NumberUtil.parseLong(params[0]),NumberUtil.parseInt(params[1]));

			ExtraParam param = new ExtraParam(3,send);

			setPrivatePoint(user,POINT_INPUT_EXTRA_PARAM,param);

			msg.send("disable_notification ？").keyboard("True","False").exec();

		} else if ("get_file".equals(function)) {

			if (params.length < 1) { invalidParams(msg,"fileId"); return; }

			sendResult(msg,execute(new SendDocument(msg.chatId(),params[0])));

		} else if ("send_file".equals(function)) {

			if (params.length < 2) { invalidParams(msg,"chatId","fileId"); return; }

			sendResult(msg,execute(new SendDocument(NumberUtil.parseLong(params[0]),params[1])));

		} else if ("send".equals(function)) {

			if (params.length < 2) { invalidParams(msg,"chatId","text..."); return; }

			Send send = new Send(this,NumberUtil.parseLong(params[0]),ArrayUtil.sub(params,1,params.length));

			ExtraParam param = new ExtraParam(5,send);

			setPrivatePoint(user,POINT_INPUT_EXTRA_PARAM,param);

			msg.send("ParseMode ？").keyboard("Html","Markdown","None").exec();

			} else if ("edit".equals(function)) {

				if (params.length < 3) { invalidParams(msg,"chatId","messageId","text..."); return; }

				Edit send = new Edit(this,NumberUtil.parseLong(params[0]),NumberUtil.parseInt(params[1]),ArrayUtil.sub(params,2,params.length));

				ExtraParam param = new ExtraParam(6,send);

				setPrivatePoint(user,POINT_INPUT_EXTRA_PARAM,param);

				msg.send("ParseMode ？").keyboard("Html","Markdown","None").exec();
			
		} else if ("unban".equals(function)) {

			if (params.length < 2) { invalidParams(msg,"chatId","userId"); return; }

			sendResult(msg,execute(new UnbanChatMember(NumberUtil.parseLong(params[0]),NumberUtil.parseInt(params[1]))));

		} else if ("unpin".equals(function)) {

			if (params.length < 1) { invalidParams(msg,"chatId"); return; }

			sendResult(msg,execute(new UnpinChatMessage(NumberUtil.parseLong(params[0]))));

		}

	}

	@Override
	public void onPoint(UserData user,Msg msg,String point,PointData data) {

		ExtraParam param = (ExtraParam) data;

		if (param.type == 0) {

			ForwardMessage send = param.data();

			if ("True".equals(msg.text())) {

				send.disableNotification(true);

			}

			clearPrivatePoint(user);

			sendResult(msg,execute(send));

		} else if (param.type == 1) {

			KickChatMember send = param.data();

			clearPrivatePoint(user);

			sendResult(msg,execute(send));

			if ("False".equals(msg.text())) {

				sendResult(msg,execute(new UnbanChatMember(send.chatId,send.userId)));

			}

		} else if (param.type == 2) {

			PromoteChatMember send = param.data();

			if ("UnPromote".equals(msg.text())) {

				send
					.canChangeInfo(false)
					.canDeleteMessages(false)
					.canEditMessages(false)
					.canInviteUsers(false)
					.canPinMessages(false)
					.canPostMessages(false)
					.canPromoteMembers(false)
					.canRestrictMembers(false);



			} else {

				send
					.canChangeInfo(true)
					.canDeleteMessages(true)
					.canEditMessages(true)
					.canInviteUsers(true)
					.canPinMessages(true)
					.canPostMessages(true)
					.canPromoteMembers(true)
					.canRestrictMembers(true);

			}

			clearPrivatePoint(user);

			sendResult(msg,execute(send));

		} else if (param.type == 3) {

			PinChatMessage send = param.data();

			if ("True".equals(msg.text())) {

				send.disableNotification(true);

			}

			clearPrivatePoint(user);

			sendResult(msg,execute(send));

		} else if (param.step == 4) {

			RestrictChatMember send = param.data();

			if (param.step == 0) {

				if ("Read Only".equals(msg.text())) {

					send.canSendMessages(false).canSendMediaMessages(false).canSendOtherMessages(false);

					clearPrivatePoint(user);

					sendResult(msg,execute(send));

					return;

				} else if ("True".equals(msg.text())) {

					send.canSendMessages(true);

				}

				param.step = 1;

				msg.send("can_send_media_messages ？").keyboard("True","False").exec();

			} else if (param.step == 1) {

				if ("True".equals(msg.text())) {

					send.canSendMediaMessages(true);

				}

				param.step = 2;

				msg.send("can_send_other_messages ？").keyboard("True","False").exec();

			} else if (param.step == 2) {

				if ("True".equals(msg.text())) {

					send.canSendOtherMessages(true);

				}

				param.step = 3;

				msg.send("can_send_media_messages ？").keyboard("True","False").exec();

			} else if (param.step == 3) {

				if ("True".equals(msg.text())) {

					send.canAddWebPagePreviews(true);

				}

				clearPrivatePoint(user);

				sendResult(msg,execute(send));

			}

		} else if (param.type == 5) {

			Send send = param.data();

			if (param.step == 0) {

				if ("Html".equals(msg.text())) {

					send.html();

				} else if ("Markdown".equals(msg.text())) {

					send.markdown();

				}


				param.step = 1;

				msg.send("disable_notification ？").keyboard("True","False").exec();

			} else if (param.step == 1) {

				if ("True".equals(msg.text())) {

					send.disableNotification();

				}

				clearPrivatePoint(user);

				sendResult(msg,send.exec());

			}

		} else if (param.type == 6) {

			Edit send = param.data();

			if ("Html".equals(msg.text())) {

				send.html();

			} else if ("Markdown".equals(msg.text())) {

				send.markdown();

			}

			clearPrivatePoint(user);

			sendResult(msg,send.exec());
			
		}

	}

	void invalidParams(Msg msg,String... params) {

		msg.send("无效的参数 , /" + msg.command() + " <" + ArrayUtil.join(params,"> <") + ">").exec();

	}

	void sendResult(Msg msg,BaseResponse resp) {

		if (resp.isOk() && resp instanceof StringResponse) {

			msg.send("OK RESULT : " + ((StringResponse)resp).result()).removeKeyboard().exec();

		} else {

			if (resp.json.length() < 1024) {

				msg.send(Html.json(resp.json)).html().exec();

			} else {

				msg.send(new JSONObject(resp.json).toStringPretty()).removeKeyboard().exec();

			}

		}

	}

}
