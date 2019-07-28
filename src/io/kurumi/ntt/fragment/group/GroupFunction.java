package io.kurumi.ntt.fragment.group;

import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.KickChatMember;
import com.pengrad.telegrambot.request.RestrictChatMember;
import com.pengrad.telegrambot.response.SendResponse;
import io.kurumi.ntt.db.GroupData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import java.util.HashMap;
import com.pengrad.telegrambot.request.*;
import cn.hutool.json.*;

public class GroupFunction extends Fragment {

	@Override
	public boolean msg() {

		return true;

	}

	public int checkMsg(UserData user,Msg msg) {

		if (!msg.isGroup()) return PROCESS_SYNC;
		else return PROCESS_ASYNC;

	}

	@Override
	public void onGroup(UserData user,Msg msg) {

		GroupData data = GroupData.get(msg.chat());

		synchronized (data) {

			if (data.delete_channel_msg != null && user.id == 777000) {

				if (data.delete_channel_msg == 0) {

					executeAsync(msg.update,new UnpinChatMessage(msg.chatId()));

				} else {

					msg.delete();

				}

			} else if (msg.message().leftChatMember() != null) {

				if (msg.message().leftChatMember().id().equals(origin.me.id())) {

					GroupData.delete(msg.chatId());
					
				} else {

				msg.delete();
				
				}


			} else if (GroupAdmin.fastAdminCheck(this,data,user.id,false)) {

				if (msg.message().newChatMembers() != null) msg.delete();

			} else if (msg.message().newChatMembers() != null) {

				User newUser = msg.message().newChatMembers()[0];

				if (newUser.id().equals(origin.me.id())) {

					return;

				}

				if (data.waitForCaptcha != null && data.waitForCaptcha.contains(user.id)) {

					msg.kick(newUser.id());

					if (newUser.isBot()) {

						if (data.invite_bot_ban != null) {

							msg.kick(true);

						} else {

							msg.kick();

						}

					} else {

						if (data.invite_user_ban != null) {

							msg.kick(true);

						} else {

							msg.kick();

						}

					}

					data.waitForCaptcha.remove(user.id);

				} else if (data.no_invite_bot != null && newUser.isBot()) {

					msg.delete();

					msg.kick(newUser.id());

					if (data.no_invite_bot != 0) {

						doRest(user,msg,data,"邀请机器人");

					}

				} else if (data.no_invite_user != null && !newUser.isBot()) {

					msg.delete();

					msg.kick(newUser.id());

					if (data.no_invite_user != 0) {

						doRest(user,msg,data,"邀请用户");

					}

				}

				if (data.delete_service_msg != null) {

					if (data.delete_service_msg == 0) {

						if (data.last_service_msg != null) {

							executeAsync(msg.update,new DeleteMessage(msg.chatId(),data.last_service_msg));

						}

						data.last_service_msg = msg.messageId();

					} else {

						data.last_service_msg = null;

						msg.delete();

					}

				}

			} else if (msg.sticker() != null && data.no_sticker != null) {

				msg.delete();

				if (data.no_sticker != 0)  {

					doRest(user,msg,data,"发送贴纸");

				}

			} else if (msg.message().photo() != null && data.no_image != null) {

				msg.delete();

				if (data.no_image != 0)  {

					doRest(user,msg,data,"发送图片");

				}


			} else if (msg.message().animation() != null && data.no_animation != null) {

				msg.delete();

				if (data.no_animation != 0)  {

					doRest(user,msg,data,"发送动图");

				}



			} else if (msg.message().audio() != null && data.no_audio != null) {

				msg.delete();

				if (data.no_audio != 0)  {

					doRest(user,msg,data,"发送音频");

				}

			} else if (msg.message().voice() != null && data.no_voice != null) {

				msg.delete();

				if (data.no_voice != 0)  {

					doRest(user,msg,data,"录制语音");

				}

			} else if (msg.message().video() != null && data.no_video != null) {

				msg.delete();

				if (data.no_video != 0)  {

					doRest(user,msg,data,"发送视频");

				}

			} else if (msg.message().videoNote() != null && data.no_video_note != null) {

				msg.delete();

				if (data.no_video_note != 0)  {

					doRest(user,msg,data,"录制视频");

				}

			} else if (msg.message().contact() != null && data.no_contact != null) {

				msg.delete();

				if (data.no_contact != 0)  {

					doRest(user,msg,data,"发送名片");

				}

			} else if (msg.message().location() != null && data.no_location != null) {

				msg.delete();

				if (data.no_location != 0)  {

					doRest(user,msg,data,"分享位置");

				}

			} else if (msg.doc() != null && data.no_file != null) {

				msg.delete();

				if (data.no_file != 0)  {

					doRest(user,msg,data,"发送文件");

				}

			}



		}

	}

	void doRest(UserData user,Msg msg,GroupData data,String name) {

		if (data.max_count != null) {

			int count;

			if (data.restWarn == null) {

				data.restWarn = new HashMap<>();

			}

			if (!data.restWarn.containsKey(user.id.toString())) {

				count = 1;

				data.restWarn.put(user.id.toString(),count);

			} else {

				count = data.restWarn.get(user.id.toString()) + 1;

				if (count == data.max_count) {

					data.restWarn.remove(user.id.toString());

				} else {

					data.restWarn.put(user.id.toString(),count);

				}

			}

			if (data.last_warn_msg != null) {

				execute(new DeleteMessage(msg.chatId(),data.last_warn_msg));

				data.last_warn_msg = null;

			}

			if (count != data.max_count) {

				SendResponse resp = msg.send(user.userName(),"\n根据群组设置 本群禁止" + name + "，你已被警告 " + count + " / " + data.max_count + " 次 ， 达到上限将被" + data.actionName() + " ！").html().exec();

				if (resp.isOk()) data.last_warn_msg = resp.message().messageId();

				return;

			}

		}

		if (data.rest_action == null && !name.startsWith("邀请")) {

			execute(new RestrictChatMember(msg.chatId(),user.id.intValue()).canSendMessages(true).canSendMediaMessages(false).canSendOtherMessages(false).canAddWebPagePreviews(false));

			if (data.last_warn_msg != null) {

				execute(new DeleteMessage(msg.chatId(),data.last_warn_msg));

			}

			SendResponse resp = msg.send(user.userName(),"\n根据群组设置 本群禁止" + name + "，你已达到警告上限并被限制发送非文本消息。","如有疑问，请联系群组管理员").html().exec();

			if (resp.isOk()) data.last_warn_msg = resp.message().messageId();

		} else if (data.rest_action == 0 && !name.startsWith("邀请")) {

			msg.restrict();

			if (data.last_warn_msg != null) {

				execute(new DeleteMessage(msg.chatId(),data.last_warn_msg));

			}

			SendResponse resp = msg.send(user.userName(),"\n根据群组设置 本群禁止" + name + "，你已达到警告上限并被禁言。","如有疑问，请联系群组管理员").html().exec();

			if (resp.isOk()) data.last_warn_msg = resp.message().messageId();

		} else {

			execute(new KickChatMember(msg.chatId(),user.id.intValue()));

			if (data.last_warn_msg != null) {

				execute(new DeleteMessage(msg.chatId(),data.last_warn_msg));

			}

			SendResponse resp = msg.send("根据群组设置 本群禁止" + name + "，" + user.userName() + " 已达到警告上限并被封锁。").html().exec();

			if (resp.isOk()) data.last_warn_msg = resp.message().messageId();

		}

	}


}
