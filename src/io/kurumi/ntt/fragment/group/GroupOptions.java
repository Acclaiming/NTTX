package io.kurumi.ntt.fragment.group;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NumberUtil;
import com.pengrad.telegrambot.request.EditMessageReplyMarkup;
import io.kurumi.ntt.db.GroupData;
import io.kurumi.ntt.db.PointData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.ButtonMarkup;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.Html;
import io.kurumi.ntt.utils.NTT;
import java.util.LinkedList;
import java.util.List;
import cn.hutool.core.util.StrUtil;
import java.util.ArrayList;

public class GroupOptions extends Fragment {

		@Override
		public void init(BotFragment origin) {

				super.init(origin);

				registerFunction("options");

				registerPoint(POINT_SET_CUST);
				
				registerCallback(
						POINT_BACK,
						POINT_MENU_MAIN,
						POINT_MENU_REST,
						POINT_MENU_JOIN,
						POINT_MENU_CUST,
						POINT_HELP,
						POINT_SET_MAIN,
						POINT_SET_REST,
						POINT_SET_JOIN,
						POINT_SET_CUST);

		}

		@Override
		public int checkFunctionContext(UserData user,Msg msg,String function,String[] params) {

				return FUNCTION_GROUP;

		}

		final String POINT_BACK = "group_main";
		final String POINT_MENU_MAIN = "group_menu_main";
		final String POINT_MENU_REST = "group_menu_rest";
		final String POINT_MENU_JOIN = "group_menu_join";
		final String POINT_MENU_CUST = "group_menu_custom";

		final String POINT_HELP = "group_help";
		final String POINT_SET_MAIN = "group_main_set";
		final String POINT_SET_REST = "group_rest_set";
		final String POINT_SET_JOIN = "group_join_set";
		final String POINT_SET_CUST = "group_custom_set";

		final class EditCustom extends PointData {

				int type;
				Callback origin;
				GroupData data;

				public EditCustom(int type,Callback origin,GroupData data) {
						this.type = type;
						this.origin = origin;
						this.data = data;
				}

				@Override
				public void onFinish() {

						origin.edit("编辑自定义问题. 对错选项或正确内容.\n",cusStats(data)).buttons(cusMenu(data)).exec();

						super.onFinish();

				}

		}

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

								 ).buttons(menuMarkup(data)).html().exec();

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


								// } else if ("enable".equals(params[0])) {


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

				synchronized (data) {

						if (POINT_BACK.equals(point)) {

								callback.edit(Html.b(data.title),Html.i("更改群组的设定")).html().buttons(menuMarkup(data)).exec();

						} else if (POINT_MENU_MAIN.equals(point)) {

								callback.edit("群组的管理设定. 点击名称查看功能说明.").buttons(mainMenu(data)).exec();

						} else if (POINT_MENU_REST.equals(point)) {

								callback.edit("限制成员进行某些操作. ","\n注意 : 当设置了 🗑 (删除) 时 不计入警告计数。\n对于禁止邀请用户/机器人 : 🗑 表示仅移除被邀请者。").buttons(restMenu(data)).exec();

						} else if (POINT_MENU_JOIN.equals(point)) {

								callback.edit("编辑群组的新成员加群验证设置. ").buttons(joinMenu(data)).exec();

						} else if (POINT_SET_MAIN.equals(point)) {

								if ("dcm".equals(params[1])) {

										if (data.delete_channel_msg == null) {

												data.delete_channel_msg = true;

												callback.text("🛠️  已开启");

										} else {

												data.delete_channel_msg = null;

												callback.text("🛠️  已关闭");

										}

								} else if ("dsm".equals(params[1])) {

										if (data.delete_service_msg == null) {

												data.delete_service_msg = true;

												callback.text("🛠️  已开启");

										} else {

												data.delete_service_msg = null;

												callback.text("🛠️  已关闭");

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

												callback.text("📝  仅移除被邀请用户");

										} else if (data.no_invite_user == 0) {

												data.no_invite_user = 1;

												callback.text("📝  移除被邀请用户并警告");


										} else {

												data.no_invite_user = null;

												callback.text("📝  不处理");

										}

								} else if ("invite_bot".equals(params[1])) {

										if (data.no_invite_bot == null) {

												data.no_invite_bot = 0;

												callback.text("📝  仅移除机器人");

										} else if (data.no_invite_bot == 0) {

												data.no_invite_bot = 1;

												callback.text("📝  移除机器人并警告");


										} else {

												data.no_invite_bot = null;

												callback.text("📝  不处理");

										}


								} else if ("sticker".equals(params[1])) {

										if (data.no_sticker == null) {

												data.no_sticker = 0;

												callback.text("📝  仅删除");

										} else if (data.no_sticker == 0) {

												data.no_sticker = 1;

												callback.text("📝  删除并警告");

										} else {

												data.no_sticker = null;

												callback.text("📝  不处理");

										}

								} else if ("image".equals(params[1])) {

										if (data.no_image == null) {

												data.no_image = 0;

												callback.text("📝  仅删除");

										} else if (data.no_image == 0) {

												data.no_image = 1;

												callback.text("📝  删除并警告");

										} else {

												data.no_image = null;

												callback.text("📝  不处理");

										}

								} else if ("animation".equals(params[1])) {

										if (data.no_animation == null) {

												data.no_animation = 0;

												callback.text("📝  仅删除");

										} else if (data.no_animation == 0) {

												data.no_animation = 1;

												callback.text("📝  删除并警告");

										} else {

												data.no_animation = null;

												callback.text("📝  不处理");

										}

								} else if ("audio".equals(params[1])) {

										if (data.no_audio == null) {

												data.no_audio = 0;

												callback.text("📝  仅删除");

										} else if (data.no_audio == 0) {

												data.no_audio = 1;

												callback.text("📝  删除并警告");

										} else {

												data.no_audio = null;

												callback.text("📝  不处理");

										}

								} else if ("video".equals(params[1])) {

										if (data.no_video == null) {

												data.no_video = 0;

												callback.text("📝  仅删除");

										} else if (data.no_video == 0) {

												data.no_video = 1;

												callback.text("📝  删除并警告");

										} else {

												data.no_video = null;

												callback.text("📝  不处理");

										}

								} else if ("video_note".equals(params[1])) {

										if (data.no_video_note == null) {

												data.no_video_note = 0;

												callback.text("📝  仅删除");

										} else if (data.no_video_note == 0) {

												data.no_video_note = 1;

												callback.text("📝  删除并警告");

										} else {

												data.no_video_note = null;

												callback.text("📝  不处理");

										}

								} else if ("contact".equals(params[1])) {

										if (data.no_contact == null) {

												data.no_contact = 0;

												callback.text("📝  仅删除");

										} else if (data.no_contact == 0) {

												data.no_contact = 1;

												callback.text("📝  删除并警告");

										} else {

												data.no_contact = null;

												callback.text("📝  不处理");

										}

								} else if ("location".equals(params[1])) {

										if (data.no_location == null) {

												data.no_location = 0;

												callback.text("📝  仅删除");

										} else if (data.no_location == 0) {

												data.no_location = 1;

												callback.text("📝  删除并警告");

										} else {

												data.no_location = null;

												callback.text("📝  不处理");

										}

								} else if ("game".equals(params[1])) {

										if (data.no_game == null) {

												data.no_game = 0;

												callback.text("📝  仅删除");

										} else if (data.no_game == 0) {

												data.no_game = 1;

												callback.text("📝  删除并警告");

										} else {

												data.no_game = null;

												callback.text("📝  不处理");

										}

								} else if ("voice".equals(params[1])) {

										if (data.no_voice == null) {

												data.no_voice = 0;

												callback.text("📝  仅删除");

										} else if (data.no_voice == 0) {

												data.no_voice = 1;

												callback.text("📝  删除并警告");

										} else {

												data.no_voice = null;

												callback.text("📝  不处理");

										}

								} else if ("file".equals(params[1])) {

										if (data.no_file == null) {

												data.no_file = 0;

												callback.text("📝  仅删除");

										} else if (data.no_file == 0) {

												data.no_file = 1;

												callback.text("📝  删除并警告");

										} else {

												data.no_file = null;

												callback.text("📝  不处理");

										}

								} else if ("action".equals(params[1])) {

										if (data.rest_action == null) {

												data.rest_action = 0;

												callback.text("📝  禁言该用户");

										} else if (data.rest_action == 0) {

												data.rest_action = 1;

												callback.text("📝  封锁该用户");

										} else {

												data.rest_action = null;

												callback.text("📝  限制非文本发送");

										}

								} else if ("inc".equals(params[1])) {

										if (data.max_count != null && data.max_count > 11) {

												callback.text("📝  新数值太高 (> 12)");

												return;

										} 

										if (data.max_count == null) {

												data.max_count = 1;

										}

										callback.text("📝  " + data.max_count + " -> " + (data.max_count = data.max_count + 1));

								} else if ("dec".equals(params[1])) {

										if (data.max_count == null) {

												callback.text("📝  再低就没了 (ﾟ⊿ﾟ)ﾂ");

												return;

										}

										callback.text("📝  " + data.max_count + " -> " + (data.max_count = data.max_count - 1));

										if (data.max_count == 1) {

												data.max_count = null;

										}

								} else {

										callback.alert("喵...？");

										return;

								}

								execute(new EditMessageReplyMarkup(callback.chatId(),callback.messageId()).replyMarkup(restMenu(data).markup()));

						} else if (POINT_SET_JOIN.equals(point)) {

								if ("enable".equals(params[1])) {

										if (data.join_captcha == null) {

												data.join_captcha = true;

												callback.text("🚪  已开启");

										} else {

												data.join_captcha = null;

												callback.text("🚪  已关闭");

										}

								} else if ("passive".equals(params[1])) {

										if (data.passive_mode == null) {

												data.passive_mode = true;

												callback.text("🚪  已开启");

										} else {

												data.passive_mode = null;

												callback.text("🚪  已关闭");

										}

								} else if ("ft_inc".equals(params[1])) {

										if (data.ft_count != null && data.ft_count >= 5) {

												callback.text("🚪  新数值太高 (> 5)");

												return;

										} 

										if (data.ft_count == null) {

												data.ft_count = 0;

										}

										callback.text("🚪  " + data.ft_count + " -> " + (data.ft_count = data.ft_count + 1));

								} else if ("captcha_del".equals(params[1])) {

										if (data.captcha_del == null) {

												data.captcha_del = 0;

												callback.text("🚪  全部延时删除");

										} else if (data.captcha_del == 0) {

												data.captcha_del = 1;

												callback.text("🚪  全部保留");

										} else {

												data.captcha_del = null;

												callback.text("🚪  保留最后一条");

										}


								} else if ("ft_dec".equals(params[1])) {

										if (data.ft_count == null) {

												callback.text("🚪  再低就没了 (ﾟ⊿ﾟ)ﾂ");

												return;

										}

										callback.text("🚪  " + data.ft_count + " -> " + (data.ft_count = data.ft_count - 1));

										if (data.ft_count == 0) {

												data.ft_count = null;

										}

								} else if ("jt_inc".equals(params[1])) {

										if (data.captcha_time != null && (data.captcha_time >= 5 * 60)) {

												callback.text("🚪  新数值太高 (> 5min)");

												return;

										} 

										if (data.captcha_time == null) {

												data.captcha_time = 50;

										}

										callback.text("🚪  " + data.parse_time() + " -> " + (data.parse_time(data.captcha_time = data.captcha_time + 10)));

										if (data.captcha_time == 50) {

												data.captcha_time = null;

										}

								} else if ("jt_inc_t".equals(params[1])) {

										if (data.captcha_time != null && (data.captcha_time >= 5 * 60)) {

												callback.text("🚪  新数值太高 (> 5min)");

												return;

										} 

										if (data.captcha_time == null) {

												data.captcha_time = 50;

										}

										int time = data.captcha_time;

										if (time + 30 > 5 * 60) {

												data.captcha_time = 5 * 60;

										} else {

												data.captcha_time = time + 30;

										}

										callback.text("🚪  " + data.parse_time(time) + " -> " + data.parse_time());

										if (data.captcha_time == 50) {

												data.captcha_time = null;

										}

								} else if ("jt_dec".equals(params[1])) {

										if (data.captcha_time != null && data.captcha_time < 21) {

												callback.text("🚪  再低还能验证吗 (ﾟ⊿ﾟ)ﾂ");

												return;

										}

										if (data.captcha_time == null) {

												data.captcha_time = 50;

										}

										callback.text("🚪  " + data.parse_time() + " -> " + data.parse_time(data.captcha_time = data.captcha_time - 10));

										if (data.captcha_time == 50) {

												data.captcha_time = null;

										}



								} else if ("jt_dec_t".equals(params[1])) {

										if (data.captcha_time != null && data.captcha_time < 21) {

												callback.text("🚪  再低还能验证吗 (ﾟ⊿ﾟ)ﾂ");

												return;

										}

										if (data.captcha_time == null) {

												data.captcha_time = 50;

										}

										int time = data.captcha_time;

										if (time - 30 > 20) {

												data.captcha_time = 20;

										} else {

												data.captcha_time = time - 30;

										}

										callback.text("🚪  " + data.parse_time(time) + " -> " + data.parse_time());

										if (data.captcha_time == 50) {

												data.captcha_time = null;

										}


								} else if ("fail_ban".equals(params[1])) {

										if (data.fail_ban == null) {

												data.fail_ban = true;

												callback.text("🚪  封锁该用户");

										} else {

												data.fail_ban = null;

												callback.text("🚪  移除该用户");

										}


								} else if ("mode_def".equals(params[1])) {

										callback.text("🚪  默认模式");

										if (data.captcha_mode == null) {

												return;

										}

										data.captcha_mode = null;

								}  else if ("mode_code".equals(params[1])) {

										callback.text("🚪  验证码验证");

										if (((Integer)0).equals(data.captcha_mode)) {

												return;

										}

										data.captcha_mode = 0;

								} else if ("mode_math".equals(params[1])) {

										callback.text("🚪  算数验证");

										if (((Integer)1).equals(data.captcha_mode)) {

												return;

										}

										data.captcha_mode = 1;

								} else if ("with_image".equals(params[1])) {

										if (data.with_image == null) {

												data.with_image = true;

												callback.text("🚪  以图片显示问题");

										} else {

												data.with_image = null;

												callback.text("🚪  以文字显示问题");

										}

								} else if ("interfere".equals(params[1])) {

										if (data.interfere == null) {

												data.interfere = true;

												callback.text("🚪  开启按钮干扰");

										} else {

												data.interfere = null;

												callback.text("🚪  关闭按钮干扰");

										}

								} else if ("require_input".equals(params[1])) {

										if (data.require_input == null) {

												if (((Integer)2).equals(data.captcha_mode) && (data.custom_a_question == null || data.custom_kw == null)) {

														callback.alert(

																"你正在使用自定义验证模式",

																"需要设定回答模式的问题与答案才能开启回答模式"

														);

														return;

												}

												data.require_input = true;

												callback.text("🚪  要求输入答案");


										} else {

												if (((Integer)2).equals(data.captcha_mode) && (data.custom_i_question == null || data.custom_items == null)) {

														callback.alert(

																"你正在使用自定义验证模式",

																"需要设定选择模式的问题与选项才能关闭回答模式"

														);

														return;

												}

												data.require_input = null;

												callback.text("🚪  要求选择答案");	

										}

								} else if ("invite_user".equals(params[1])) {

										if (data.invite_user_ban == null) {

												data.invite_user_ban = true;

												callback.text("🚪  封锁");

										} else {

												data.invite_user_ban = null;

												callback.text("🚪  移除");

										}

								} else if ("invite_bot".equals(params[1])) {

										if (data.invite_bot_ban == null) {

												data.invite_bot_ban = true;

												callback.text("🚪  封锁");

										} else {

												data.invite_bot_ban = null;

												callback.text("🚪  移除");

										}

								} else if ("mode_cus".equals(params[1])) {

										callback.edit("编辑自定义问题. 对错选项或正确内容.\n",cusStats(data)).buttons(cusMenu(data)).exec();

										return;

								} else {

										callback.alert("喵...？");

										return;

								}

								execute(new EditMessageReplyMarkup(callback.chatId(),callback.messageId()).replyMarkup(joinMenu(data).markup()));

						} else if (POINT_SET_CUST.equals(point)) {

								if ("enable_cus".equals(params[1])) {

										if (((Integer)2).equals(data.captcha_mode)) {

												callback.text("🚪  已关闭");

												data.captcha_mode = null;

										} else if (data.require_input == null && (data.custom_i_question == null || data.custom_items == null)) {

												callback.alert(

														"你正在使用选项模式",

														"需要设定选项模式的问题与选项才能继续"


												);
												
												return;

										} else if (data.require_input != null && (data.custom_a_question == null || data.custom_kw == null)) {

												callback.alert(

														"你正在使用回答模式",

														"需要设定回答模式的问题与正确回答才能继续"


												);

												return;

										}	else {

												callback.text("🚪  已开启");

												data.captcha_mode = 2;

										}

										execute(new EditMessageReplyMarkup(callback.chatId(),callback.message().messageId()).replyMarkup(cusMenu(data).markup()));

								} else if ("reset_i_question".equals(params[1])) {

										callback.confirm();

										EditCustom edit = new EditCustom(0,callback,data);

										callback.send("现在发送问题 :").exec(edit);

										setPrivatePoint(user,POINT_SET_CUST,edit);
										
								} else if ("reset_items".equals(params[1])) {

										callback.confirm();

										EditCustom edit = new EditCustom(1,callback,data);

										callback.send("现在发送选项 每行一个 至少一个 最多六个 正确答案以 + 号开头 :").exec(edit);

										setPrivatePoint(user,POINT_SET_CUST,edit);

								} else if ("reset_a_question".equals(params[1])) {

										callback.confirm();

										EditCustom edit = new EditCustom(2,callback,data);

										callback.send("现在发送问题 :").exec(edit);
										
										setPrivatePoint(user,POINT_SET_CUST,edit);
										
								} else if ("reset_answer".equals(params[1])) {

										callback.confirm();

										EditCustom edit = new EditCustom(3,callback,data);

										callback.send("现在发送正确关键字 每行一个 :").exec(edit);

										setPrivatePoint(user,POINT_SET_CUST,edit);
										
								} 
								
						}

				}

		}

		@Override
		public void onPoint(UserData user,Msg msg,String point,PointData data) {

				EditCustom edit = (EditCustom)data.with(msg);

				if (edit.type == 0) {

						if (!msg.hasText()) {

								msg.send("请输入新的选择模式问题 :").withCancel().exec(data);

								return;

						}

						edit.data.custom_i_question = msg.text();

						clearPrivatePoint(user);

				} else if (edit.type == 1) {

						if (!msg.hasText()) {

								msg.send("请输入新的选择模式选项 :").withCancel().exec(data);

								return;

						}

						List<GroupData.CustomItem> items = new LinkedList<>();

						boolean valid = false;

						ArrayList<String> buttons = new ArrayList<>();
						
						for (final String line : msg.text().split("\n")) {

								if (buttons.contains(line)) {
										
										msg.send("选项重复 : " + line).withCancel().exec(data);
										
										return;
									
								}
								
								
								
								if (line.startsWith("+")) {
										
										buttons.add(line.substring(1));
										
										valid = true;

										items.add(new GroupData.CustomItem() {{

																this.isValid = true;
																this.text = line.substring(1);

														}});

								} else {
										
										buttons.add(line);

										items.add(new GroupData.CustomItem() {{

																this.isValid = false;
																this.text = line;

														}});

								}

					  }

						if (items.isEmpty()) {

								msg.send("选项为空 请重试").withCancel().exec(data);

								return;

						} else if (items.size() > 6) {

								msg.send("选项太多 (> 6)").exec(data);

								return;

						} else if (!valid) {

								msg.send("没有包含一个正确选项","再说一遍 : 每行一个选项，正确选项以+开头").exec(data);

								return;

						}

						edit.data.custom_items = items;

						clearPrivatePoint(user);

				} else if (edit.type == 2) {

						if (!msg.hasText()) {

								msg.send("请输入新的回答模式问题 :").withCancel().exec(data);

								return;

						}

						edit.data.custom_a_question = msg.text();

						clearPrivatePoint(user);

				} else if (edit.type == 0) {

						if (StrUtil.isBlank(msg.text())) {

								msg.send("请输入新的选择模式答案 :").withCancel().exec(data);

								return;

						}

						LinkedList<String> custom_kw = new LinkedList<>();

						for (String kw : msg.text().split("\n")) {

								if (!StrUtil.isBlank(kw)) {

										edit.data.custom_kw.add(kw);

								}

						}

						if (custom_kw.isEmpty()) {

								msg.send("选项为空 请重试！").withCancel().exec(data);

								return;

						}

						edit.data.custom_kw = custom_kw;

						clearPrivatePoint(user);

				}


		}

		ButtonMarkup menuMarkup(final GroupData data) {

				return new ButtonMarkup() {{

								newButtonLine("🛠️  功能选项",POINT_MENU_MAIN,data.id);
								newButtonLine("📝  成员限制",POINT_MENU_REST,data.id);
								newButtonLine("🚪  加群验证",POINT_MENU_JOIN,data.id);

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

								newButtonLine("警告 " + (data.max_count == null ? 1 : data.max_count) + " 次 : " + data.actionName(),POINT_SET_REST,data.id,"action");

								newButtonLine().newButton("➖",POINT_SET_REST,data.id,"dec").newButton("➕",POINT_SET_REST,data.id,"inc");

								newButtonLine("🔙",POINT_BACK,data.id);

						}};


		}

		ButtonMarkup joinMenu(final GroupData data) {

				return new ButtonMarkup() {{

								newButtonLine()
										.newButton("开启审核",POINT_HELP,"enable")
										.newButton(data.join_captcha != null ? "✅" : "☑",POINT_SET_JOIN,data.id,"enable");

								newButtonLine()
										.newButton("被动模式",POINT_HELP,"passive")
										.newButton(data.passive_mode != null ? "✅" : "☑",POINT_SET_JOIN,data.id,"passive");

								newButtonLine("容错次数 : " + (data.ft_count == null ? 0 : data.ft_count),"null");

								newButtonLine().newButton("➖",POINT_SET_JOIN,data.id,"ft_dec").newButton("➕",POINT_SET_JOIN,data.id,"ft_inc");

								newButtonLine("时间上限 : " + data.parse_time(),"null");

								newButtonLine()
										.newButton("➖",POINT_SET_JOIN,data.id,"jt_dec")
										.newButton("➖➖",POINT_SET_JOIN,data.id,"jt_dec_t")
										.newButton("➕",POINT_SET_JOIN,data.id,"jt_inc")
										.newButton("➕➕",POINT_SET_JOIN,data.id,"jt_inc_t");

								newButtonLine()
										.newButton("验证失败",POINT_HELP,"fail_ban")
										.newButton(data.fail_ban == null ? "移除" : "封锁",POINT_SET_JOIN,data.id,"fail_ban");

								newButtonLine()
										.newButton("保留验证消息",POINT_HELP,"captcha_del")
										.newButton(data.captcha_del == null ? "最后一条" : data.captcha_del == 0 ? "不保留" : "全部保留",POINT_SET_JOIN,data.id,"captcha_del");


								newButtonLine("验证期间邀请用户",POINT_HELP,"invite_when_captcha");

								newButtonLine()
										.newButton("邀请用户",POINT_HELP,"invite_user")
										.newButton(data.invite_user_ban == null ? "移除" : "封锁",POINT_SET_JOIN,data.id,"invite_user");

								newButtonLine()
										.newButton("邀请机器人",POINT_HELP,"invite_bot")
										.newButton(data.invite_bot_ban == null ? "移除" : "封锁",POINT_SET_JOIN,data.id,"invite_bot");

								newButtonLine("审核模式","null");

								newButtonLine()
										.newButton("默认模式",POINT_HELP,"mode_def")
										.newButton(data.captcha_mode == null ? "●" : "○",POINT_SET_JOIN,data.id,"mode_def");

								newButtonLine()
										.newButton("验证码",POINT_HELP,"mode_code")
										.newButton(((Integer)0).equals(data.captcha_mode) ? "●" : "○",POINT_SET_JOIN,data.id,"mode_code");

								newButtonLine()
										.newButton("算数题",POINT_HELP,"mode_math")
										.newButton(((Integer)1).equals(data.captcha_mode) ? "●" : "○",POINT_SET_JOIN,data.id,"mode_math");

								newButtonLine()
										.newButton("自定义",POINT_HELP,"mode_cus")
										.newButton(((Integer)2).equals(data.captcha_mode) ? "●" : "○",POINT_SET_JOIN,data.id,"mode_cus");

								newButtonLine()
										.newButton("图片描述",POINT_HELP,"with_image")
										.newButton(data.with_image != null ? "✅" : "☑",POINT_SET_JOIN,data.id,"with_image");

								newButtonLine()
										.newButton("干扰按钮",POINT_HELP,"interfere")
										.newButton(data.interfere != null ? "✅" : "☑",POINT_SET_JOIN,data.id,"interfere");

								newButtonLine()
										.newButton("回答模式",POINT_HELP,"require_input")
										.newButton(data.require_input != null ? "✅" : "☑",POINT_SET_JOIN,data.id,"require_input");

								/*

								 newButtonLine("预设配置","null");

								 newButtonLine("简易",POINT_SET_JOIN,data.id,"easy");
								 newButtonLine("一般 ",POINT_SET_JOIN,data.id,"base");
								 newButtonLine("最严 ",POINT_SET_JOIN,data.id,"hard");
								 newButtonLine("重置所有配置",POINT_SET_JOIN,data.id,"reset");

								 */

								/*

								 newButtonLine()
								 .newButton("定制模式",POINT_HELP,"mode_cus")
								 .newButton(((Integer)1).equals(data.captcha_mode)? "●" : "○",POINT_SET_JOIN,data.id,"mode_cus");

								 */

								newButtonLine("🔙",POINT_BACK,data.id);

						}};

		}

		String cusStats(GroupData data) {

				StringBuilder stats = new StringBuilder();

				stats.append("选择模式问题 : ");

				if (data.custom_i_question == null) {

						stats.append("未设定");

				} else {

						stats.append(data.custom_i_question);

				}

				stats.append("\n选择模式选项 : ");

				if (data.custom_items == null) {

						stats.append("未设定");

				} else {

						stats.append("\n").append(ArrayUtil.join(data.custom_items.toArray(),"\n"));

				}

				stats.append("\n\n");

				stats.append("回答模式问题 : ");

				if (data.custom_a_question == null) {

						stats.append("未设定");

				} else {

						stats.append(data.custom_a_question);

				}

				stats.append("\n正确关键字 : ");

				if (data.custom_kw == null) {

						stats.append("未设定");

				} else {

						stats.append(ArrayUtil.join(data.custom_kw.toArray(),"\n"));

				}

				return stats.toString();

		}

		ButtonMarkup cusMenu(final GroupData data) {

				return new ButtonMarkup() {{

								newButtonLine()
										.newButton("使用自定义问题",POINT_HELP,"enable_cus")
										.newButton(((Integer)2).equals(data.captcha_mode) ? "✅" : "☑",POINT_SET_CUST,data.id,"enable_cus");

								newButtonLine("设置选择模式问题",POINT_SET_CUST,data.id,"reset_i_question");
								newButtonLine("设置选择模式选项",POINT_SET_CUST,data.id,"reset_items");

								newButtonLine("设置回答模式问题",POINT_SET_CUST,data.id,"reset_a_question");
								newButtonLine("设置回答模式答案",POINT_SET_CUST,data.id,"reset_answer");

								newButtonLine("🔙",POINT_MENU_JOIN,data.id);

						}};

		}



}
