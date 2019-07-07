package io.kurumi.ntt.fragment.bots;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.ForwardMessage;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendSticker;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.Html;
import java.util.LinkedList;
import java.util.List;
import io.kurumi.ntt.db.PointData;

public class ForwardBot extends BotFragment {

    final String POINT_REPLY = "r";

    public Long botId;
    public Long userId;
    public String botToken;
    public Long lastReceivedFrom;
    public String welcomeMessage;
    public String userName;
    public List<Long> blockList;

    @Override
    public void reload() {

		super.reload();

        UserBot bot = UserBot.data.getById(botId);

        botToken = bot.token;

        userId = bot.user;

        welcomeMessage = (String) bot.params.get("msg");

        blockList = (List<Long>) bot.params.get("block");

        if (blockList == null) {

            blockList = new LinkedList<>();

        }

        UserData user = UserData.get(userId);

        if (user == null) {

            userName = "(" + userId + ")";

        } else {

            userName = user.name();

        }

    }

    public void save() {

        UserBot bot = UserBot.data.getById(botId);

        bot.params.put("block",blockList);

        UserBot.data.setById(botId,bot);

    }

    @Override
    public String botName() {

        return "Forward Bot For " + userName;

    }

    @Override
    public String getToken() {

        return botToken;

    }

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerFunction("start");

	}

	@Override
	public int onBlockedMsg(UserData user,Msg msg) {
		
		return 2;
		
	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		super.onFunction(user,msg,function,params);

		if ("start".equals(function)) {

			msg.send(welcomeMessage).exec();

		} else if (!functions.containsKey(function)) {
			
			checkMsg(user,msg);
			
		}
		
	}

	@Override
	public void onPayload(UserData user,Msg msg,String payload,String[] params) {

		if ("reply".equals(payload)) {

			UserData target = UserData.get(Long.parseLong(params[0]));

			if (target == null) {

				msg.send("找不到目标...").failedWith();

				return;

			}

			msg.send("回复 " + target.userName() + " : ","直接发送信息即可 (非文本，表情，文件 会直接转发) : ","使用 /cancel 退出").html().exec();

			setPrivatePointData(user,POINT_REPLY,target.id);

		} else if ("del".equals(payload)) {

			try {

				long target = Long.parseLong(params[0]);
				int messageId = Integer.parseInt(params[1]);

				BaseResponse resp = bot().execute(new DeleteMessage(target,messageId));

				if (resp.isOk()) {

					msg.send("已删除").failedWith();
					
				} else {

					msg.send("删除失败 这条发送的信息还在吗 ？").failedWith();

				}

			} catch (NumberFormatException e) {

				msg.send("这个删除已经点过了 :)").failedWith();

			}

		} else if ("block".equals(payload)) {

			UserData target = UserData.get(Long.parseLong(params[0]));

			if (target == null) {

				msg.send("找不到目标...").failedWith();

				return;

			}

			if (target.id.equals(userId)) {

				msg.send("你不能屏蔽你自己...").failedWith();

				return;

			}

			if (blockList.contains(target.id.longValue())) {

				msg.send("已经屏蔽过了 " + target.userName() + " ~ [ " + Html.a("解除屏蔽","https://t.me/" + me.username() + "?start=unblok" + PAYLOAD_SPLIT + target.id) + " ] ~").html().exec();

			} else {

				blockList.add(user.id);
				save();
				msg.send("已屏蔽 " + target.userName() + " ~ [ " + Html.a("解除屏蔽","https://t.me/" + me.username() + "?start=unblock" + PAYLOAD_SPLIT + target.id) + " ] ~").html().exec();


			}

		} else if ("unblock".equals(payload)) {

			UserData target = UserData.get(Long.parseLong(params[0]));

			if (target == null) {

				msg.send("找不到目标...").failedWith();

				return;

			}

			if (blockList.contains(target.id.longValue())) {

				blockList.remove(target.id.longValue());

				save();

				msg.send("已解除屏蔽 " + target.userName() + " ~ [ " + Html.a("屏蔽","https://t.me/" + me.username() + "?start=block" + PAYLOAD_SPLIT + target.id) + " " + Html.a("发送消息","https://t.me/" + me.username() + "?start=reply" + PAYLOAD_SPLIT + user.id) + " ]").html().exec();

			} else {

				msg.send("没有屏蔽 " + target.userName() + " ~ [ " + Html.a("屏蔽","https://t.me/" + me.username() + "?start=block" + PAYLOAD_SPLIT + target.id) + " " + Html.a("发送消息","https://t.me/" + me.username() + "?start=reply" + PAYLOAD_SPLIT + user.id) + " ]").html().exec();


			}

		}


	}

	@Override
	public int checkMsg(UserData user,Msg msg) {

		if (userId.equals(user.id) || !blockList.contains(user.id.longValue())) {

            if (lastReceivedFrom == null || !lastReceivedFrom.equals(user.id)) {

                new Send(this,userId,"来自 " + user.userName() + " : [ " + Html.a("回复","https://t.me/" + me.username() + "?start=reply" + PAYLOAD_SPLIT + user.id) + " " + Html.a("屏蔽","https://t.me/" + me.username() + "?start=block" + PAYLOAD_SPLIT + user.id) + " ]").html().exec();

                lastReceivedFrom = user.id;

            }

            msg.forwardTo(userId);

        } else {

			onFinalMsg(user,msg);

		}

		return PROCESS_REJECT;

    }

	@Override
	public void onPoint(UserData user,Msg msg,String point,PointData data) {

        long target = (long) data.data;

        if (POINT_REPLY.equals(point)) {

            Message message = msg.message();

            int sended = -1;

            if (message.document() != null) {

                SendDocument send = new SendDocument(target,message.document().fileId());

                send.fileName(message.document().fileName());

                send.caption(message.text());

                SendResponse resp = bot().execute(send);

                if (!resp.isOk()) {

                    msg.send("发送失败 (˚☐˚! )/","-----------------------",resp.description()).exec();

                } else {

                    sended = resp.message().messageId();

                }

            } else if (message.sticker() != null) {

                SendSticker send = new SendSticker(target,message.sticker().fileId());

                SendResponse resp = bot().execute(send);

                if (!resp.isOk()) {

                    msg.send("发送失败 (˚☐˚! )/","-----------------------",resp.description()).exec();

                } else {

                    sended = resp.message().messageId();

                }

            } else if (msg.hasText()) {

                SendMessage send = new SendMessage(target,msg.text());

                SendResponse resp = bot().execute(send);

                if (!resp.isOk()) {

                    msg.send("发送失败 (˚☐˚! )/","-----------------------",resp.description()).exec();

                } else {

                    sended = resp.message().messageId();

                }

            } else {

                ForwardMessage forward = new ForwardMessage(target,msg.chatId(),msg.messageId());

                SendResponse resp = bot().execute(forward);

                if (!resp.isOk()) {

                    msg.send("发送失败 (˚☐˚! )/","-----------------------",resp.description()).exec();

                } else {

                    sended = resp.message().messageId();

                }

            }

            if (sended != -1) {

                msg.reply("发送成功 [ " + Html.a("删除","https://t.me/" + me.username() + "?start=del" + PAYLOAD_SPLIT + target + PAYLOAD_SPLIT + sended) + " ]","退出回复使用 /cancel ").html().exec();

            }


        }

    }

}
