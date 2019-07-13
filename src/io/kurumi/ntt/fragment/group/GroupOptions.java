package io.kurumi.ntt.fragment.group;

import cn.hutool.core.util.NumberUtil;
import com.pengrad.telegrambot.request.EditMessageReplyMarkup;
import io.kurumi.ntt.db.GroupData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.ButtonMarkup;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.Html;
import io.kurumi.ntt.utils.NTT;
import java.util.concurrent.atomic.AtomicInteger;

public class GroupOptions extends Fragment {

		@Override
		public void init(BotFragment origin) {

				super.init(origin);

				registerFunction("options");

				registerCallback(
						POINT_BACK,
						POINT_MENU_MAIN,
						POINT_MENU_REST,
						POINT_HELP,
						POINT_SET_MAIN,
						POINT_SET_REST);

		}

		@Override
		public int checkFunctionContext(UserData user,Msg msg,String function,String[] params) {

				return FUNCTION_GROUP;

		}

		final String POINT_BACK = "group_main";
		final String POINT_MENU_MAIN = "group_menu_main";
		final String POINT_MENU_REST = "group_menu_rest";

		final String POINT_HELP = "group_help";
		final String POINT_SET_MAIN = "group_main_set";
		final String POINT_SET_REST = "group_rest_set";

		@Override
		public void onFunction(UserData user,final Msg msg,String function,String[] params) {

				if (NTT.checkGroupAdmin(msg)) return;

				final GroupData data = GroupData.get(msg.chat());

				if (!user.contactable()) {

						msg.send("请主动给BOT发送一条消息 : 通常是使用 /start 按钮。"," ( 因为BOT不能主动发送私聊消息 )").exec();

						return;

				}

				new Send(user.id,

                 Html.b(msg.chat().title()),
								 Html.i("更改群组的设定")

								 ).buttons(new ButtonMarkup() {{

                    newButtonLine("🛠️  功能选项",POINT_MENU_MAIN,msg.chatId());
										newButtonLine("📝 成员限制",POINT_MENU_REST,data.id);

								}}).html().exec();

			  msg.reply("已经通过私聊发送群组设置选项").failedWith();

		}

    @Override
    public void onCallback(UserData user,Callback callback,String point,String[] params) {

				if (POINT_HELP.equals(point)) {

						if ("dcm".equals(params[0])) {

								callback.alert(

										"删除来自绑定的频道的消息 :\n",

										"如果群组作为频道绑定的讨论群组，则每条频道消息都会被转发至群组并置顶。\n",

										"开启此功能自动删除来自频道的消息。"

								);

						} else if ("dsm".equals(params[0])) {

								callback.alert(

										"删除服务消息 :\n",

										"服务消息 (Service Message) 指 : 成员加群、被邀请、退群、被移除。\n",

										"开启此功能自动删除服务消息。"

								);

						} else {

								callback.alert("喵....？");

						}

						return;

				}

				final GroupData data = GroupData.data.getById(NumberUtil.parseLong(params[0]));

				if (data == null) {

						callback.alert("Error","无效的目标群组");

						return;

				}

				if (POINT_BACK.equals(point)) {

						callback.edit(Html.b(data.title),Html.i("更改群组的设定")).html().buttons(menuMarkup(data)).exec();

				} else if (POINT_MENU_MAIN.equals(point)) {

						callback.edit("群组的管理设定. 点击名称查看功能说明.").buttons(mainMenu(data)).exec();

				} else if (POINT_MENU_REST.equals(point)) {


						callback.message().audio();

						callback.edit("限制成员进行某些操作. ","\n注意 : 当设置了 🗑 (删除) 时 不计入警告计数。\n对于禁止邀请用户/机器人 : 🗑 表示仅移除被邀请者。").buttons(restMenu(data)).exec();

				} else if (POINT_SET_MAIN.equals(point)) {

						if ("dcm".equals(params[1])) {

								if (data.delete_channel_msg == null) {

										data.delete_channel_msg = true;

										callback.text("已开启 ~");

								} else {

										data.delete_channel_msg = null;

										callback.text("已关闭 ~");

								}

						} else if ("dsm".equals(params[1])) {

								if (data.delete_service_msg == null) {

										data.delete_service_msg = true;

										callback.text("已开启 ~");

								} else {

										data.delete_service_msg = null;

										callback.text("已关闭 ~");

								}



						} else {

								callback.alert("喵...？");

								return;

						}

						execute(new EditMessageReplyMarkup(callback.chatId(),callback.messageId()).replyMarkup(mainMenu(data).markup()));

				} else if (POINT_SET_REST.equals(point)) {

						if ("invite_user".equals(params[1])) {

								if (data.no_invite_user == null) {

										data.no_invite_user = 0;

										callback.text("📝 仅移除被邀请用户");

								} else if (data.no_invite_user == 0) {

										data.no_invite_user = 1;

										callback.text("📝 移除被邀请用户并警告");


								} else {

										data.no_invite_user = null;

										callback.text("📝 不处理");

								}

						} else if ("invite_bot".equals(params[1])) {

								if (data.no_invite_bot == null) {

										data.no_invite_bot = 0;

										callback.text("📝 仅移除机器人");

								} else if (data.no_invite_bot == 0) {

										data.no_invite_bot = 1;

										callback.text("📝 移除机器人并警告");


								} else {

										data.no_invite_bot = null;

										callback.text("📝 不处理");

								}


						} else if ("sticker".equals(params[1])) {

								if (data.no_sticker == null) {

										data.no_sticker = 0;

										callback.text("📝 仅删除");

								} else if (data.no_sticker == 0) {

										data.no_sticker = 1;

										callback.text("📝 删除并警告");

								} else {

										data.no_sticker = null;

										callback.text("📝 不处理");

								}

						} else if ("image".equals(params[1])) {

								if (data.no_image == null) {

										data.no_image = 0;

										callback.text("📝 仅删除");

								} else if (data.no_image == 0) {

										data.no_image = 1;

										callback.text("📝 删除并警告");

								} else {

										data.no_image = null;

										callback.text("📝 不处理");

								}

						} else if ("animation".equals(params[1])) {

								if (data.no_animation == null) {

										data.no_animation = 0;

										callback.text("📝 仅删除");

								} else if (data.no_animation == 0) {

										data.no_animation = 1;

										callback.text("📝 删除并警告");

								} else {

										data.no_animation = null;

										callback.text("📝 不处理");

								}

						} else if ("audio".equals(params[1])) {

								if (data.no_audio == null) {

										data.no_audio = 0;

										callback.text("📝 仅删除");

								} else if (data.no_audio == 0) {

										data.no_audio = 1;

										callback.text("📝 删除并警告");

								} else {

										data.no_audio = null;

										callback.text("📝 不处理");

								}

						} else if ("video".equals(params[1])) {

								if (data.no_video == null) {

										data.no_video = 0;

										callback.text("📝 仅删除");

								} else if (data.no_video == 0) {

										data.no_video = 1;

										callback.text("📝 删除并警告");

								} else {

										data.no_video = null;

										callback.text("📝 不处理");

								}

						} else if ("video_note".equals(params[1])) {

								if (data.no_video_note == null) {

										data.no_video_note = 0;

										callback.text("📝 仅删除");

								} else if (data.no_video_note == 0) {

										data.no_video_note = 1;

										callback.text("📝 删除并警告");

								} else {

										data.no_video_note = null;

										callback.text("📝 不处理");

								}

						} else if ("contact".equals(params[1])) {

								if (data.no_contact == null) {

										data.no_contact = 0;

										callback.text("📝 仅删除");

								} else if (data.no_image == 0) {

										data.no_contact = 1;

										callback.text("📝 删除并警告");

								} else {

										data.no_contact = null;

										callback.text("📝 不处理");

								}

						} else if ("location".equals(params[1])) {

								if (data.no_location == null) {

										data.no_location = 0;

										callback.text("📝 仅删除");

								} else if (data.no_location == 0) {

										data.no_location = 1;

										callback.text("📝 删除并警告");

								} else {

										data.no_location = null;

										callback.text("📝 不处理");

								}

						} else if ("game".equals(params[1])) {

								if (data.no_game == null) {

										data.no_game = 0;

										callback.text("📝 仅删除");

								} else if (data.no_game == 0) {

										data.no_game = 1;

										callback.text("📝 删除并警告");

								} else {

										data.no_game = null;

										callback.text("📝 不处理");

								}

						} else if ("voice".equals(params[1])) {

								if (data.no_voice == null) {

										data.no_voice = 0;

										callback.text("📝 仅删除");

								} else if (data.no_voice == 0) {

										data.no_voice = 1;

										callback.text("📝 删除并警告");

								} else {

										data.no_voice = null;

										callback.text("📝 不处理");

								}

						} else if ("file".equals(params[1])) {

								if (data.no_file == null) {

										data.no_file = 0;

										callback.text("📝 仅删除");

								} else if (data.no_file == 0) {

										data.no_file = 1;

										callback.text("📝 删除并警告");

								} else {

										data.no_file = null;

										callback.text("📝 不处理");

								}

						} else if ("action".equals(params[1])) {

								if (data.rest_action == null) {

										data.rest_action = 0;

										callback.text("📝 禁言该用户");

								} else if (data.rest_action == 0) {

										data.rest_action = 1;

										callback.text("📝 封锁该用户");

								} else {

										data.rest_action = null;

										callback.text("📝 限制非文本发送");

								}

						} else if ("inc".equals(params[1])) {

								if (data.max_count != null && data.max_count > 11) {

										callback.text("📝 新数值太高 (> 12)");

										return;

								} 

								if (data.max_count == null) {

										data.max_count = 1;

								}

								callback.text("📝 " + data.max_count + " -> " + (data.max_count = data.max_count + 1));

						} else {

								callback.alert("喵...？");

								return;

						}

						execute(new EditMessageReplyMarkup(callback.chatId(),callback.messageId()).replyMarkup(restMenu(data).markup()));


				}

		}

		ButtonMarkup menuMarkup(final GroupData data) {

				return new ButtonMarkup() {{

								newButtonLine("🛠️  功能选项",POINT_MENU_MAIN,data.id);
								newButtonLine("📝 成员限制",POINT_MENU_REST,data.id);

						}};


		}

		ButtonMarkup mainMenu(final GroupData data) {

				return new ButtonMarkup() {{

								newButtonLine()
										.newButton("删除频道消息",POINT_HELP,"dcm")
										.newButton(data.delete_channel_msg != null ? "✅" : "☑",POINT_SET_MAIN,data.id,"dcm");

								newButtonLine()
										.newButton("删除服务消息",POINT_HELP,"dsm")
										.newButton(data.delete_service_msg != null ? "✅" : "☑",POINT_SET_MAIN,data.id,"dsm");


								newButtonLine("🔙",POINT_BACK,data.id);

						}};

		}

		ButtonMarkup restMenu(final GroupData data) {

				return new ButtonMarkup() {{

								newButtonLine()
										.newButton("邀请新成员",POINT_HELP,"invite_user")
										.newButton(data.no_invite_user == null ? "✅" : data.no_invite_user == 0 ? "🗑" : "❌",POINT_SET_REST,data.id,"invite_user");

								newButtonLine()
										.newButton("邀请机器人",POINT_HELP,"invite_bot")
										.newButton(data.no_invite_bot == null ? "✅" : data.no_invite_bot == 0 ? "🗑" : "❌",POINT_SET_REST,data.id,"invite_bot");

								newButtonLine()
										.newButton("发送贴纸",POINT_HELP,"sticker")
										.newButton(data.no_sticker == null ? "✅" : data.no_sticker == 0 ? "🗑" : "❌",POINT_SET_REST,data.id,"sticker");

								newButtonLine()
										.newButton("发送图片",POINT_HELP,"image")
										.newButton(data.no_image == null ? "✅" : data.no_image == 0 ? "🗑" : "❌",POINT_SET_REST,data.id,"image");

								newButtonLine()
										.newButton("发送动图",POINT_HELP,"animation")
										.newButton(data.no_animation == null ? "✅" : data.no_animation == 0 ? "🗑" : "❌",POINT_SET_REST,data.id,"animation");

								newButtonLine()
										.newButton("发送音频",POINT_HELP,"audio")
										.newButton(data.no_audio == null ? "✅" : data.no_audio == 0 ? "🗑" : "❌",POINT_SET_REST,data.id,"audio");

								newButtonLine()
										.newButton("录制语音",POINT_HELP,"voice")
										.newButton(data.no_voice == null ? "✅" : data.no_voice == 0 ? "🗑" : "❌",POINT_SET_REST,data.id,"voice");

								newButtonLine()
										.newButton("发送视频",POINT_HELP,"video")
										.newButton(data.no_video == null ? "✅" : data.no_video == 0 ? "🗑" : "❌",POINT_SET_REST,data.id,"video");

								newButtonLine()
										.newButton("录制视频",POINT_HELP,"video_note")
										.newButton(data.no_video_note == null ? "✅" : data.no_video_note == 0 ? "🗑" : "❌",POINT_SET_REST,data.id,"video_note");

								newButtonLine()
										.newButton("发送名片",POINT_HELP,"contact")
										.newButton(data.no_contact == null ? "✅" : data.no_contact == 0 ? "🗑" : "❌",POINT_SET_REST,data.id,"contact");

								newButtonLine()
										.newButton("发送位置",POINT_HELP,"location")
										.newButton(data.no_location == null ? "✅" : data.no_location == 0 ? "🗑" : "❌",POINT_SET_REST,data.id,"location");

								newButtonLine()
										.newButton("发送游戏",POINT_HELP,"game")
										.newButton(data.no_game == null ? "✅" : data.no_game == 0 ? "🗑" : "❌",POINT_SET_REST,data.id,"game");

								newButtonLine()
										.newButton("发送文件",POINT_HELP,"file")
										.newButton(data.no_file == null ? "✅" : data.no_file == 0 ? "🗑" : "❌",POINT_SET_REST,data.id,"file");

								newButtonLine("警告 " + (data.max_count == null ? 1 : data.max_count) + " 次 " + data.actionName(),POINT_SET_REST,"action");

								newButtonLine().newButton("➕",POINT_SET_REST,"inc").newButton("➖","dec");

								newButtonLine("🔙",POINT_BACK,data.id);

						}};


		}



}
