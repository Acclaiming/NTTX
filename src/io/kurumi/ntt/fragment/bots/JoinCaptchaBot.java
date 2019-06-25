package io.kurumi.ntt.fragment.bots;

import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.GetChatMember;
import com.pengrad.telegrambot.response.GetChatMemberResponse;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.abs.Callback;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.fragment.abs.request.ButtonMarkup;
import io.kurumi.ntt.fragment.abs.request.Send;
import io.kurumi.ntt.utils.Html;
import io.kurumi.ntt.utils.NTT;

import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import io.kurumi.ntt.fragment.admin.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.fragment.group.*;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.response.SendResponse;

public class JoinCaptchaBot extends BotFragment {

    static Timer timer = new Timer();

    final String POINT_AUTH = "auth";

	final String POINT_ACC = "acc";
	final String POINT_REJ = "rej";

    public Long botId;
    public Long userId;
    public String botToken;
    public String userName;
    public Long logChannel;
    public Boolean delJoin;
    HashMap<Long, HashMap<Long, Msg>> cache = new HashMap<>();

    String welcomeMessage;
    Integer lastWelcomeMessage;

    boolean lastChanged = false;

    Boolean deleteLastWelcome;

    @Override
    public void reload() {

		super.reload();

        UserBot bot = UserBot.data.getById(botId);

        botToken = bot.token;

        userId = bot.user;

        UserData user = UserData.get(userId);

        if (user == null) {

            userName = "(" + userId + ")";

        } else {

            userName = user.name();

        }

        delJoin = (Boolean) bot.params.get("delJoin");

        if (delJoin == null) delJoin = false;

        logChannel = (Long) bot.params.get("logChannel");

        welcomeMessage = (String)bot.params.get("welcome");

        lastWelcomeMessage = (Integer)bot.params.get("last");

        deleteLastWelcome = (Boolean)bot.params.get("delLast");

    }

    @Override
    public String botName() {

        return "Join Captcha Bot For " + userName;

    }

    @Override
    public String getToken() {

        return botToken;

    }

	@Override
	public boolean onPrivate(UserData user,Msg msg) {

		msg.send("喵.... ？").exec();

		return true;

	}

    @Override
    public boolean onGroup(UserData user,final Msg msg) {

		if (user.developer()) return false;

        if (msg.message().groupChatCreated() != null || msg.message().supergroupChatCreated() != null) {

            msg.send("欢迎使用由 @NTT_X 驱动的开源加群验证BOT","给BOT 删除消息 和 封禁用户 权限就可以使用了 ~").exec();

        } else if (msg.message().leftChatMember() != null) {

            if (cache.containsKey(msg.chatId())) {

                HashMap<Long, Msg> group = cache.get(msg.chatId());

                if (group.containsKey(msg.message().leftChatMember().id())) {

                    group.remove(msg.message().leftChatMember().id()).delete();

                    if (group.isEmpty()) cache.remove(msg.chatId());

                }

            }

            if (delJoin) msg.delete();

            if (user.id.equals(me.id())) {

                msg.delete();

                return true;

            } else if (!user.id.equals(msg.message().leftChatMember().id())) {

                return true;

            }

            UserData left = UserData.get(msg.message().leftChatMember());

            if (logChannel != null) {

                new Send(this,logChannel,"事件 : #成员退出","群组 : " + msg.chat().title(),"[" + Html.code(msg.chatId().toString()) + "]","用户 : " + left.userName(),"#id" + left.id).html().exec();

            }

        } else if (msg.message().newChatMember() != null || msg.message().newChatMembers() != null) {

            if (delJoin) msg.delete();

            final HashMap<Long, Msg> group = cache.containsKey(msg.chatId()) ? cache.get(msg.chatId()) : new HashMap<Long, Msg>();

            User newMember = msg.message().newChatMember();

            if (newMember == null) newMember = msg.message().newChatMembers()[0];

            if (newMember.isBot()) {

                if (newMember.id().equals(botId)) {

					msg.send("欢迎使用由 @NTT_X 驱动的开源加群验证BOT","给BOT 删除消息 和 封禁用户 权限就可以使用了 ~").exec();

                }

                return false;

            }

            final UserData newData = UserData.get(newMember);

			if (Firewall.block.containsId(newData.id)) {

				if (msg.kick() && logChannel != null) {

					new Send(this,logChannel,"事件 : #未通过 #SPAM","群组 : " + msg.chat().title(),"[" + Html.code(msg.chatId().toString()) + "]","用户 : " + user.userName(),"#id" + user.id).html().exec();

					return true;

				}


			} else if (AntiEsu.keywordMatch(newData.name())) {

				if (msg.kick() && logChannel != null) {

					new Send(this,logChannel,"事件 : #未通过 #ESU","群组 : " + msg.chat().title(),"[" + Html.code(msg.chatId().toString()) + "]","用户 : " + user.userName(),"#id" + user.id).html().exec();

					return true;

				}

			}

			if (!newMember.isBot() && ((System.currentTimeMillis() / 1000) - msg.message().date()) > 10 * 1000) {

				msg.send("你好呀，新来的绒布球 " + newData.userName() + " 因为咱处理超时，就算乃通过验证了 )").html().exec();

				return true;

			}

            String[] info = new String[]{

				"你好呀，新加裙的绒布球 " + newData.userName() + " ~\n",

				"现在需要确认一下乃是不是机器人绒布球了 ~\n",

				"发送 喵 (嘤也可以 就可以通过验证了 ~ 3分钟以内呀 (๑˃̵ᴗ˂̵)و \n",

				"注意不要点按钮 喵 ~"

            };


            ButtonMarkup buttons = new ButtonMarkup() {{

					newButtonLine()
                        .newButton("不要",POINT_AUTH,newData.id)
                        .newButton("点",POINT_AUTH,newData.id)
                        .newButton("按钮",POINT_AUTH,newData.id)
                        .newButton("喵",POINT_AUTH,newData.id);

					newButtonLine()
						.newButton(" ※ 通过 ※ ",POINT_ACC,newData.id)
						.newButton(" ※ 滥权 ※ ",POINT_REJ,newData.id);

				}};

            setPoint(newData,POINT_AUTH,PointStore.Type.Group);

            group.put(newMember.id(),msg.send(info).buttons(buttons).html().send());

            cache.put(msg.chatId().longValue(),group);

            timer.schedule(new TimerTask() {

					@Override
					public void run() {

						final HashMap<Long, Msg> group = cache.containsKey(msg.chatId()) ? cache.get(msg.chatId()) : new HashMap<Long, Msg>();

						if (group.containsKey(newData.id.longValue())) {

							clearPoint(newData);

							group.remove(newData.id).delete();

							if (group.isEmpty()) {

								cache.remove(msg.chatId());

							} else {

								cache.put(msg.chatId().longValue(),group);

							}

							if (msg.kick(newData.id)) {

								msg.send(newData.userName() + " 不理解喵喵的语言 , 真可惜喵...").html().failed(60 * 1000);

								if (logChannel != null) {

									new Send(origin,logChannel,"事件 : #未通过 #超时","群组 : " + msg.chat().title(),"[" + Html.code(msg.chatId().toString()) + "]","用户 : " + newData.userName(),"#id" + newData.id).html().exec();

								}

							}

						}

					}

				},new Date(System.currentTimeMillis() + 3 * 60 * 1000));

        }

        return true;

    }

    @Override
    public boolean onCallback(UserData user,Callback callback) {

        long target = Long.parseLong(callback.params[1]);
		HashMap<Long, Msg> group = cache.containsKey(callback.chatId()) ? cache.get(callback.chatId()) : new HashMap<Long, Msg>();

		String point = callback.params[0];

		if (POINT_AUTH.equals(point)) {

			if (!user.id.equals(target)) {

				callback.alert("这个验证不针对乃 ~");

				return true;

			}

			if (!group.containsKey(user.id)) {

				callback.alert("这个验证已失效 (");
				callback.delete();

				return true;

			}


			if (!user.id.equals(target)) {

				callback.alert("这个验证不针对乃 /");

				return true;

			}

			group.remove(user.id).delete();

			if (group.isEmpty()) {

				cache.remove(callback.chatId());

			} else {

				cache.put(callback.chatId().longValue(),group);

			}

			clearPoint(user);

			if (callback.kick(user.id)) {

				callback.send(user.userName() + " 瞎按按钮 , 未通过验证 , 真可惜喵...").html().failed(60 * 1000);

				if (logChannel != null) {

					new Send(this,logChannel,"事件 : #未通过 #点击按钮","群组 : " + callback.chat().title(),"[" + Html.code(callback.chatId().toString()) + "]","用户 : " + user.userName(),"#id" + user.id).html().exec();

				}


			}


		} else if (POINT_ACC.equals(point)) {

			if (NTT.checkGroupAdmin(callback)) return true;

			if (group.containsKey(target)) {

				group.remove(target).delete();

			}

			point().points.remove(target);

			UserData targetUser = UserData.get(target);

			callback.send(targetUser.userName() + " py了管理之后通过了验证喵...").html().failed(15 * 1000);

            sendWelcome(user,callback);

			if (logChannel != null) {

				new Send(this,logChannel,"事件 : #通过验证 #管理员通过","群组 : " + callback.chat().title(),"[" + Html.code(callback.chatId().toString()) + "]","用户 : " + targetUser.userName(),"#id" + target).html().exec();

			}


		} else if (POINT_REJ.equals(point)) {

			if (NTT.checkGroupAdmin(callback)) return true;

			if (group.containsKey(target)) {

				group.remove(target).delete();

			}

			point().points.remove(target);

			UserData targetUser = UserData.get(target);

			if (callback.kick(target)) {

				callback.send(targetUser.userName() + " 被滥权了喵...").html().failed(15 * 1000);

				if (logChannel != null) {

					new Send(this,logChannel,"事件 : #未通过 #管理员移除","群组 : " + callback.chat().title(),"[" + Html.code(callback.chatId().toString()) + "]","用户 : " + targetUser.userName(),"#id" + target).html().exec();

				}

			}


		}

		return true;

	}

    void sendWelcome(UserData user,Msg msg) {

        if (welcomeMessage != null) {

            if (lastWelcomeMessage != null) {

                bot().execute(new DeleteMessage(msg.chatId(),lastWelcomeMessage));

            }

            SendResponse resp = msg.send(welcomeMessage.replace("$新成员",user.userName())).html().exec();

            if (resp.isOk()) {

                lastWelcomeMessage = resp.message().messageId();

                lastChanged = true;

            }

        }

    }

    @Override
    public void stop() {

        if (lastChanged) {

            UserBot bot = UserBot.data.getById(botId);

            bot.params.put("last",lastWelcomeMessage);

            UserBot.data.setById(botId,bot);

        }

        super.stop();

    }

    @Override
    public boolean onPointedGroup(UserData user,Msg msg) {

		clearPoint(user);

        HashMap<Long, Msg> group = cache.containsKey(msg.chatId()) ? cache.get(msg.chatId()) : new HashMap<Long, Msg>();

		if (cache.containsKey(msg.chatId())) {

			if (group.containsKey(msg.message().leftChatMember().id())) {

				group.remove(msg.message().leftChatMember().id()).delete();

				if (group.isEmpty()) cache.remove(msg.chatId());

			}

		}

		if (msg.message().leftChatMember() != null) {

			if (delJoin) msg.delete();

			if (user.id.equals(me.id())) {

				msg.delete();

				return true;

			} else if (!user.id.equals(msg.message().leftChatMember().id())) {

				return true;

			}

			UserData left = UserData.get(msg.message().leftChatMember());

			if (logChannel != null) {

				new Send(this,logChannel,"事件 : #成员退出","群组 : " + msg.chat().title(),"[" + Html.code(msg.chatId().toString()) + "]","用户 : " + left.userName(),"#id" + left.id).html().exec();

			}

		} else {

			if (msg.hasText() && (msg.text().contains("喵") || msg.text().contains("嘤"))) {

				msg.send(user.userName() + " 通过了图灵(划掉) 验证 ~").html().failed(15 * 1000);

				sendWelcome(user,msg);

				if (logChannel != null) {

					new Send(this,logChannel,"事件 : #通过验证","群组 : " + msg.chat().title(),"[" + Html.code(msg.chatId().toString()) + "]","用户 : " + user.userName(),"#id" + user.id).html().exec();

				}

			} else if (msg.kick()) {

				msg.delete();

				msg.send(user.userName() + " 不懂喵喵的语言 , 真可惜喵...").html().failed(60 * 1000);

				if (logChannel != null) {

					new Send(this,logChannel,"事件 : #未通过 #发送其他内容","群组 : " + msg.chat().title(),"[" + Html.code(msg.chatId().toString()) + "]","用户 : " + user.userName(),"#id" + user.id).html().exec();

				}

			}


		}
		
		return true;

	}


}
