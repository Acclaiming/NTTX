package io.kurumi.ntt.fragment.group;

import io.kurumi.ntt.db.GroupData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.ButtonMarkup;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.NTT;
import cn.hutool.core.util.NumberUtil;
import com.pengrad.telegrambot.request.EditMessageReplyMarkup;

public class GroupOptions extends Fragment {

		@Override
		public void init(BotFragment origin) {

				super.init(origin);

				registerFunction("options");
				
				registerCallback(POINT_SHOW_HELP,POINT_SWITCH_CF);

		}

		@Override
		public int checkFunctionContext(UserData user,Msg msg,String function,String[] params) {
				
				return FUNCTION_GROUP;
				
			}

		final String POINT_SHOW_HELP = "group_help";
		final String POINT_SWITCH_CF = "group_config";

		@Override
		public void onFunction(UserData user,final Msg msg,String function,String[] params) {

				if (NTT.checkGroupAdmin(msg)) return;

				final GroupData data = GroupData.get(msg.chat());

				if (!user.contactable()) {

						msg.send("请主动给BOT发送一条消息 : 通常是使用 /start 按钮。"," ( 因为BOT不能主动发送私聊消息 )").exec();

						return;

				}

				new Send(user.id,

                 "这里是群组设置选单为 : " + msg.chatId()

                 ).buttons(new ButtonMarkup() {{

                    newButtonLine()
												.newButton("删除频道信息",POINT_SHOW_HELP,"dcm")
												.newButton(" [ > - < ] ",POINT_SHOW_HELP,"center")
												.newButton(data.delete_channel_msg == null ? "开启" : "关闭",POINT_SWITCH_CF,msg.chatId(),"dcm");

                }}).exec();

		}

    @Override
    public void onCallback(UserData user,Callback callback,String point,String[] params) {

        if (POINT_SHOW_HELP.equals(point)) {

            if ("center".equals(params[0])) {

                callback.alert(

                    "点击左边的功能可以查看设置说明",

                    "点击右边的按钮更改设置",

                    "(*σ´∀`)σ"

                );

            } else if ("dcm".equals(params[0])) {

                callback.alert(

                    "删除来自绑定的频道的消息 :\n",

                    "如果群组作为频道绑定的讨论群组，则每条频道消息都会被转发至群组并置顶。\n",

                    "开启此功能自动删除来自频道的消息。"

                );

            } else {

								callback.alert("喵....？");

						}

        } else if (POINT_SWITCH_CF.equals(point)) {

						final GroupData data = GroupData.data.getById(NumberUtil.parseLong(params[0]));

						if (data == null) {

								callback.alert("Error","无效的目标群组");

								return;

						}

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
												.newButton("删除频道信息",POINT_SHOW_HELP,"dcm")
												.newButton(" [ > - < ] ",POINT_SHOW_HELP,"center")
												.newButton(data.delete_channel_msg == null ? "开启" : "关闭",POINT_SWITCH_CF,data.id,"dcm");

								}};

						execute(new EditMessageReplyMarkup(callback.chatId(),callback.messageId()).replyMarkup(buttons.markup()));


				}

    }

}
