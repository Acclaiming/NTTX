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

public class GroupOptions extends Fragment {

		@Override
		public void init(BotFragment origin) {

				super.init(origin);

				registerFunction("options");

				registerCallback(
						POINT_BACK,
						POINT_MENU_MAIN,
						POINT_HELP,
						POINT_SET);

		}

		@Override
		public int checkFunctionContext(UserData user,Msg msg,String function,String[] params) {

				return FUNCTION_GROUP;

		}

		final String POINT_BACK = "group_main";
		final String POINT_MENU_MAIN = "group_menu_main";

		final String POINT_HELP = "group_help";
		final String POINT_SET = "group_set";

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
								 Html.I("更改群组的设定")

								 ).buttons(new ButtonMarkup() {{

                    newButtonLine("🛠️ 功能选项",POINT_MENU_MAIN,msg.chatId());

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

						ButtonMarkup buttons = new ButtonMarkup() {{

										newButtonLine("🛠️ 功能选项",POINT_MENU_MAIN,data.id);

								}};

						callback.edit(Html.b(data.title),Html.I("更改群组的设定")).html().buttons(buttons).exec();

				} else if (POINT_MENU_MAIN.equals(point)) {

						ButtonMarkup buttons = new ButtonMarkup() {{

										newButtonLine()
												.newButton("删除频道消息",POINT_HELP,"dcm")
												.newButton(data.delete_channel_msg == null ? "✅" : "☑",POINT_SET,data.id,"dcm");

										newButtonLine("🔙",POINT_BACK,data.id);

								}};

						callback.edit("群组的管理设定. 点击名称查看功能说明.").buttons(buttons).exec();

				} else if (POINT_SET.equals(point)) {

						if ("dcm".equals(params[1])) {

								if (data.delete_channel_msg == null) {

										data.delete_channel_msg = true;

										callback.text("已开启 ~");

								} else {

										data.delete_channel_msg = null;

										callback.text("已关闭 ~");

								}

						} else {

								callback.alert("喵...？");

								return;

						}

						ButtonMarkup buttons = new ButtonMarkup() {{

										newButtonLine()
												.newButton("删除频道消息",POINT_HELP,"dcm")
												.newButton(data.delete_channel_msg == null ? "✅" : "☑",POINT_SET,data.id,"dcm");

										newButtonLine("🔙",POINT_BACK,data.id);

								}};

						execute(new EditMessageReplyMarkup(callback.inlineMessgeId()).replyMarkup(buttons.markup()));

				}

		}

}
