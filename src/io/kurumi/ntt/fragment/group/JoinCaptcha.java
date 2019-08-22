package io.kurumi.ntt.fragment.group;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.KickChatMember;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.request.SendSticker;
import com.pengrad.telegrambot.response.SendResponse;
import io.kurumi.ntt.db.GroupData;
import io.kurumi.ntt.db.PointData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.admin.Firewall;
import io.kurumi.ntt.fragment.group.codes.BaseCode;
import io.kurumi.ntt.fragment.group.codes.CustomCode;
import io.kurumi.ntt.fragment.group.codes.MathCode;
import io.kurumi.ntt.fragment.group.codes.StringCode;
import io.kurumi.ntt.fragment.group.codes.VerifyCode;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.ButtonMarkup;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.Img;
import io.kurumi.ntt.utils.NTT;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.regex.Pattern;
import com.pengrad.telegrambot.request.GetChatMember;
import com.pengrad.telegrambot.response.GetChatMemberResponse;

public class JoinCaptcha extends Fragment {

    public static Pattern arabicCharacter = Pattern.compile("\\x{0600}-\\x{06FF}\\x{0750}-\\x{077F}\\x{08A0}-\\x{08FF}\\x{FB50}-\\x{FDFF}\\x{FE70}-\\x{FEFF}\\x{10E60}-\\x{10E7F}\\x{1EC70}-\\x{1ECBF}\\x{1ED00}-\\x{1ED4F}\\x{1EE00}-\\x{1EEFF}");

    // static Pattern arabicCharacter = Pattern.compile("\\p{Arabic}",Pattern.UNICODE_CHARACTER_CLASS);

    final String POINT_AUTH = "join_auth";
    final String POINT_INTERFERE = "join_interfere";
    final String POINT_ANSWER = "join_answer";
    final String POINT_ACC = "join_acc";
    final String POINT_REJ = "join_rej";
    final String POINT_DELETE = "join_del";

    HashMap<Long, HashMap<Long, AuthCache>> cache = new HashMap<>();

    public void onStop() {

        for (Map.Entry<Long, HashMap<Long, AuthCache>> g : cache.entrySet()) {

            GroupData data = GroupData.data.getById(g.getKey());

            for (Map.Entry<Long, AuthCache> c : g.getValue().entrySet()) {

                AuthCache auth = c.getValue();

                final UserData user = UserData.get(c.getKey());

				if (user == null) continue;

                auth.task.cancel();

                if (auth.authMsg != null) {

                    auth.authMsg.delete();

                    auth.authMsg.restrict(user.id);

                    if (data.passive_msg != null && data.passive_msg.containsKey(user.id.toString())) {

                        int id = data.passive_msg.remove(user.id.toString());

                        execute(new DeleteMessage(data.id,id));

                    }

                    if (data.captchaFailed != null) {

                        data.captchaFailed.remove(user.id.toString());

                        if (data.captchaFailed.isEmpty()) data.captchaFailed = null;

                    }

                    SendResponse resp = new Send(this,data.id,"你好，新成员 " + user.userName() + "\n因为服务中断，已将你暂时禁言。请点击下方重新验证。")
						.buttons(new ButtonMarkup() {{

                                newButtonLine("开始验证",POINT_AUTH,user.id);

                                newButtonLine().newButton("通过",POINT_ACC,user.id).newButton("滥权",POINT_REJ,user.id);

                            }}).html().exec();

                    if (resp.isOk()) {

                        if (data.passive_msg == null) data.passive_msg = new HashMap<>();

						Integer last = data.passive_msg.put(user.id.toString(),resp.message().messageId());

						if (last != null) {

							execute(new DeleteMessage(data.id,last));

						}

                    }

                }

            }

        }

    }

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerCallback(POINT_AUTH,POINT_DELETE,POINT_ANSWER,POINT_ACC,POINT_REJ);
		registerPoint(POINT_AUTH,POINT_DELETE,POINT_ANSWER);
		
    }

	@Override
	public int checkMsg(UserData user,Msg msg) {

		return PROCESS_SYNC;

	}

    @Override
    public void onMsg(final UserData user,Msg msg) {

        if (!msg.isSuperGroup()) return;

        GroupData data = GroupData.get(msg.chat());

        if (msg.message().newChatMembers() != null) {

            User newMember = msg.message().newChatMembers()[0];

            final UserData newData = UserData.get(newMember);

            if (data.anti_halal != null) {

                if (ReUtil.isMatch(arabicCharacter,newData.name())) {

                    msg.delete();

                    msg.kick();

                    return;

                }

            }

            if (data.backhole != null) {

                if (Firewall.block.containsId(newData.id)) {

                    msg.kick(true);

                    msg.delete();

                    return;

                }

            }

            if (data.join_captcha == null) {

                if (data.cas_spam != null) {

                    HttpResponse result = HttpUtil.createGet("https://combot.org/api/cas/check?user_id=" + newData.id).execute();

                    if (result != null && result.isOk()) {

                        if (new JSONObject(result.body()).getBool("ok",false)) {

                            msg.restrict();

                            msg.send(newData.userName() + " 在 Combot Anit-Spam 黑名单内，已禁言。\n详情请查看 : https://combot.org/cas/query?u=" + newData.id).html().async();

							return;

                        }

                    }

                }

                if (data.welcome == null) return;

                if (data.del_welcome_msg != null) {

                    if (data.last_welcome_msg != null) {

                        executeAsync(msg.update,deleteMessage(data.id,data.last_welcome_msg));

                    }

                    if (data.last_welcome_msg_2 != null) {

                        executeAsync(msg.update,new DeleteMessage(data.id,data.last_welcome_msg_2));

                    }

                }

                if (data.welcome == 0) {

                    if (!((Integer) 1).equals(data.delete_service_msg)) {

                        if (data.del_welcome_msg == null) {

                            msg.reply(data.welcomeMessage).html().async();

                        } else {

                            SendResponse resp = msg.reply(data.welcomeMessage).html().exec();

                            if (resp != null && resp.isOk()) {

                                data.last_welcome_msg = resp.message().messageId();

                            }

                        }

                    } else {

                        if (data.del_welcome_msg == null) {

                            msg.send(user.userName() + " , " + data.welcomeMessage).html().async();

                        } else {

                            SendResponse resp = msg.send(user.userName() + " , " + data.welcomeMessage).html().exec();

                            if (resp != null && resp.isOk()) {

                                data.last_welcome_msg = resp.message().messageId();

                            }

                        }

                    }

                } else {

                    String sticker = data.welcomeSet.get(RandomUtil.randomInt(0,data.welcomeSet.size()));

                    if (!((Integer) 1).equals(data.delete_service_msg)) {

                        if (data.del_welcome_msg == null) {

                            executeAsync(msg.update,new SendSticker(data.id,sticker).replyToMessageId(msg.messageId()));

                            if (data.welcome == 2) {

                                msg.send(data.welcomeMessage).html().async();

                            }

                        } else {

                            SendResponse resp = execute(new SendSticker(data.id,sticker).replyToMessageId(msg.messageId()));

                            if (resp != null && resp.isOk()) {

                                data.last_welcome_msg = resp.message().messageId();

                            }

                            if (data.welcome == 2) {

                                resp = msg.send(data.welcomeMessage).html().exec();

                                if (resp != null && resp.isOk()) {

                                    data.last_welcome_msg_2 = resp.message().messageId();

                                }

                            }

                        }


                    } else {

                        if (data.del_welcome_msg == null) {

                            if (data.welcome == 2) {

                                msg.send(user.userName() + " , " + data.welcomeMessage).html().async();

                                executeAsync(msg.update,new SendSticker(data.id,sticker));

                            }

                        } else {

                            SendResponse resp = msg.send(user.userName() + " , " + data.welcomeMessage).html().exec();

                            if (resp != null && resp.isOk()) {

                                data.last_welcome_msg = resp.message().messageId();

                            }

                            resp = execute(new SendSticker(data.id,sticker));

                            if (resp != null && resp.isOk()) {

                                data.last_welcome_msg = resp.message().messageId();


                            }

                        }


                    }

                }


            } else {

                if (!user.id.equals(newData.id)) return;

                if (data.waitForCaptcha == null) {

                    data.waitForCaptcha = new ArrayList<>();

                    data.waitForCaptcha.add(user.id);

                } else if (!data.waitForCaptcha.contains(user.id)) {

                    data.waitForCaptcha.add(user.id);

                }

                if (data.passive_mode != null) {

                    msg.restrict();

                    if (data.delete_service_msg != null) {

                        SendResponse resp = msg.send("你好，新成员 " + newData.userName() + " 为确保群组安全，已将你暂时禁言。请点击下方按钮开始验证。")
							.buttons(new ButtonMarkup() {{

                                    newButtonLine("开始验证",POINT_AUTH,user.id);

                                    newButtonLine().newButton("通过",POINT_ACC,user.id).newButton("滥权",POINT_REJ,user.id);

                                }}).html().exec();

                        if (resp.isOk()) {

                            if (data.passive_msg == null) data.passive_msg = new HashMap<>();
                            data.passive_msg.put(user.id.toString(),resp.message().messageId());

                        }

                    } else {

                        SendResponse resp = msg.reply("新成员你好，为确保群组安全，已将你暂时禁言。请点击下方按钮开始验证。")
							.buttons(new ButtonMarkup() {{

                                    newButtonLine("开始验证",POINT_AUTH,user.id);

                                    newButtonLine().newButton("通过",POINT_ACC,user.id).newButton("滥权",POINT_REJ,user.id);

                                }}).exec();

                        if (resp.isOk()) {

                            if (data.passive_msg == null) data.passive_msg = new HashMap<>();
                            data.passive_msg.put(user.id.toString(),resp.message().messageId());

                        }

                    }

                    return;

                }

                startAuth(user,msg,data,null);

            }

        } else if (msg.message().leftChatMember() != null) {

            final HashMap<Long, AuthCache> group = cache.containsKey(msg.chatId()) ? cache.get(msg.chatId()) : new HashMap<Long, AuthCache>();

            User newMember = msg.message().leftChatMember();

            final UserData newData = UserData.get(newMember);

            if (group.containsKey(newData.id)) {

                msg.delete();

                failed(user,msg,group.get(newData.id),data,"主动退群");

            }

        } else if (data.waitForCaptcha != null && data.waitForCaptcha.contains(user.id)) {

            data.waitForCaptcha.remove(user.id);

            // 管理员取消 禁言

        }

    }

    class AuthCache extends PointData {

        UserData user;
        Msg authMsg;
        Msg serviceMsg;

        boolean input;
        VerifyCode code;

        TimerTask task;

    }

    void startAuth(final UserData user,final Msg msg,final GroupData data,VerifyCode left) {

        final HashMap<Long, AuthCache> group = cache.containsKey(msg.chatId()) ? cache.get(msg.chatId()) : new HashMap<Long, AuthCache>();

        setGroupPoint(user,POINT_DELETE);

        final VerifyCode code;

        if (left != null) {

            code = left.fork();

        } else if (data.captcha_mode == null) {

            code = new BaseCode(data.require_input != null);

        } else if (data.captcha_mode == 0) {

            code = new StringCode(data.require_input != null);

        } else if (data.captcha_mode == 1) {

            code = new MathCode(data.require_input != null,data.with_image != null);

        } else {

			if (data.require_input != null && data.custom_kw == null) {

				data.captcha_mode = null;

				code = new BaseCode(data.require_input != null);

			} else {

				code = new CustomCode(data.require_input != null,data);

			}

        }

        ButtonMarkup buttons = new ButtonMarkup() {{

				if (data.interfere != null) {

					newButtonLine()
                        .newButton("□",POINT_INTERFERE,user.id)
                        .newButton("□",POINT_INTERFERE,user.id)
                        .newButton("□",POINT_INTERFERE,user.id)
                        .newButton("□",POINT_INTERFERE,user.id);

				}

				if (data.require_input == null) {

					List<String> all = new LinkedList<>();

					for (String interfere : code.invalidCode()) all.add(interfere);

					if (code.validCode() != null) all.add(code.validCode());

					Collections.shuffle(all);

					for (String show : all) newButtonLine(show,POINT_ANSWER,user.id,show);

				}

				newButtonLine()
                    .newButton("通过",POINT_ACC,user.id)
                    .newButton("滥权",POINT_REJ,user.id);

				if (data.interfere != null) {

					newButtonLine()
                        .newButton("■",POINT_INTERFERE,user.id)
                        .newButton("■",POINT_INTERFERE,user.id)
                        .newButton("■",POINT_INTERFERE,user.id)
                        .newButton("■",POINT_INTERFERE,user.id);

				}

			}};


        final AuthCache auth = new AuthCache();

        auth.user = user;

        auth.input = data.require_input != null;
        auth.code = code;

        if (msg.message().newChatMembers() != null) auth.serviceMsg = msg;

		clearGroupPoint(user);

        if (data.with_image == null) {

            if (auth.input) {

                msg.unrestrict();

                setGroupPoint(user,POINT_ANSWER,auth);

            } else {

                msg.restrict();

                clearGroupPoint(user);

            }

            if (code.code() == null) {

                if (left != null) {

                    auth.authMsg = msg.send(user.userName() + " , 请重试 : 你有 " + data.parse_time() + " 的时间\n\n" + code.question()).buttons(buttons).html().send();

                } else {

                    auth.authMsg = msg.send(user.userName() + " , 你有 " + data.parse_time() + " 的时间\n\n" + code.question()).buttons(buttons).html().send();


                }

            } else {

                if (left != null) {

                    auth.authMsg = msg.send(user.userName() + " , 请重试  : 你有 " + data.parse_time() + " 的时间\n\n" + code.question() + "\n\n" + code.code()).buttons(buttons).html().send();

                } else {

                    auth.authMsg = msg.send(user.userName() + " , 请验证  : 你有 " + data.parse_time() + " 的时间\n\n" + code.question() + "\n\n" + code.code()).buttons(buttons).html().send();

                }

            }

            if (auth.authMsg == null) return;

            AuthCache old = group.put(user.id,auth);

            if (old != null) {

                old.authMsg.delete();
                old.task.cancel();

            }

        } else {

            Img info = new Img(1000,600,Color.WHITE);

            info.drawLineInterfere(50);

            info.font("Noto Sans CJK SC Thin",39);

            if (code.code() != null) {

                info.drawRandomColorTextCenter(0,0,0,400,left == null ? "请验证 :)" : "请重试 :)");
                info.drawRandomColorTextCenter(0,200,0,200,code.code());
                info.drawRandomColorTextCenter(0,400,0,0,code.question());

            } else {

                info.drawRandomColorTextCenter(0,0,0,300,left == null ? "请验证 :)" : "请重试 :)");
                info.drawRandomColorTextCenter(0,300,0,0,code.question());

            }

            SendResponse resp = execute(new SendPhoto(msg.chatId(),info.getBytes()).caption(user.userName() + " 你有 " + data.parse_time() + " 的时间").parseMode(ParseMode.HTML).replyMarkup(buttons.markup()));

            if (resp != null && resp.isOk()) {

                auth.authMsg = new Msg(this,resp.message());

            }

            if (auth.authMsg == null) {

                clearGroupPoint(user);

            }

            if (auth.input) {

                msg.unrestrict();

                setGroupPoint(user,POINT_ANSWER,auth);

            } else {

                msg.restrict();

                clearGroupPoint(user);

            }

            AuthCache old = group.put(user.id,auth);

            if (old != null) {

                old.authMsg.delete();
                old.task.cancel();

            }


        }

        auth.task = new TimerTask() {

            @Override
            public void run() {

                if (!group.containsKey(user.id)) return;

                failed(user,msg,auth,data,true,"超时");

            }

        };

        cache.put(msg.chatId(),group);

        BotFragment.mainTimer.schedule(auth.task,new Date(System.currentTimeMillis() + ((data.captcha_time == null ? 50 : data.captcha_time) * 1000)));

    }

    @Override
    public void onPoint(final UserData user,Msg msg,String point,PointData data) {

        if (POINT_DELETE.equals(point)) {
			
            msg.delete();
			
            return;
			
        }

        final GroupData gd = GroupData.get(msg.chat());

        final AuthCache auth = (AuthCache) data;

        if (msg.message().leftChatMember() != null) {

            failed(user,msg,auth,gd,"主动退群");

            return;

        } else if (msg.message().newChatMembers() != null && msg.message().newChatMembers().length != 0) {

            User newMember = msg.message().newChatMembers()[0];

            if (user.id.equals(newMember.id())) {

                startAuth(user,msg,gd,null);

                return;

            }

            executeAsync(msg.update,new KickChatMember(msg.chatId(),newMember.id().intValue()));

            if (newMember.isBot()) {

                if (gd.invite_bot_ban != null) {

                    msg.kick(true);

                } else {

                    msg.kick();

                }

            } else {

                if (gd.invite_user_ban != null) {

                    msg.kick(true);

                } else {

                    msg.kick();

                }

            }

            failed(user,msg,auth,gd,true,"验证期间邀请用户");

            return;

        }

        if (auth.code.verify(msg.text())) {

            success(user,msg,auth,gd,null);

        } else {

            failed(user,msg,auth,gd,"验证失败");

        }

    }

    @Override
    public void onCallback(final UserData user,Callback callback,String point,String[] params) {

        if (params.length == 0) return;

        long target = NumberUtil.parseInt(params[0]);

        if (POINT_AUTH.equals(point)) {

            if (!user.id.equals(target)) {

                callback.alert("这个验证不针对你。");

                return;

            }

            callback.delete();

            startAuth(user,callback,GroupData.get(callback.chat()),null);

            return;

        }

        final HashMap<Long, AuthCache> group = cache.containsKey(callback.chatId()) ? cache.get(callback.chatId()) : new HashMap<Long, AuthCache>();
        final GroupData gd = GroupData.get(callback.chat());
        AuthCache auth = group.get(user.id);

        if (POINT_INTERFERE.equals(point)) {

            if (!user.id.equals(target)) {

                callback.alert("这个验证不针对你。");

                return;

            }

            failed(user,callback,auth,gd,"点击按钮");

        } else if (POINT_ACC.equals(point) || POINT_REJ.equals(point)) {

            if (!NTT.checkGroupAdmin(callback)) {

				if (POINT_ACC.equals(point)) {

					success(UserData.get(target),callback,auth,gd,"管理员通过");

				} else {

					failed(UserData.get(target),callback,auth,gd,true,"点击按钮");

				}

			} else if (user.id.equals(target)) {

				failed(user,callback,auth,gd,"点击按钮");

            }

        } else if (POINT_ANSWER.equals(point)) {

            if (!user.id.equals(target)) {

                callback.alert("这个验证不针对你。");

                return;

            }


            if (auth == null) {

                callback.delete();

                callback.restrict(user.id);

                callback.send(user.userName() + " , 验证丢失。 你可以重试 :)")
					.buttons(new ButtonMarkup() {{

                            newButtonLine("重新验证",POINT_AUTH,user.id);

                            newButtonLine().newButton("通过",POINT_ACC,user.id).newButton("滥权",POINT_REJ,user.id);

                        }}).html().exec();

                return;

            }

            if (auth.code.verify(params[1])) {

                success(user,callback,auth,gd,null);

            } else {

                failed(user,callback,auth,gd,"验证失败");

            }

        }

    }

    void success(UserData user,Msg msg,AuthCache auth,GroupData gd,String cause) {

        if (cache.containsKey(msg.chatId())) {

            AuthCache rdc = cache.get(msg.chatId()).remove(user.id);

            if (auth == null) auth = rdc;

        }

        if (auth != null) {

            if (auth.authMsg != null) auth.authMsg.delete();

            auth.task.cancel();

        }

        msg.delete();

        gd.waitForCaptcha.remove(user.id);

        if (gd.require_input == null) {

            msg.unrestrict();

        }

        if (!(msg instanceof Callback)) {

            clearGroupPoint(user);

        }

        if (gd.captcha_del != null && gd.last_join_msg != null) {

            execute(new DeleteMessage(msg.chatId(),gd.last_join_msg));

            gd.last_join_msg = null;

        }

        if (gd.cas_spam != null) {

			HttpResponse result = HttpUtil.createGet("https://combot.org/api/cas/check?user_id=" + user.id).execute();

			if (result != null && result.isOk()) {

				if (new JSONObject(result.body()).getBool("ok",false)) {

                    msg.kick(true);

                    msg.send(user.userName() + " 在 Combot Anit-Spam 黑名单内，已封锁。\n详情请查看 : https://combot.org/cas/query?u=" + user.id).html().async();

                    return;

                }

            }

        }
		
		GetChatMemberResponse target = execute(new GetChatMember(gd.id,user.id.intValue()));

		if (!target.isOk() || !target.chatMember().isMember()) {

			return;

		}
		

        if (gd.captcha_del == null) {

            msg.send(user.userName() + " 欢迎加入 ~").html().failed();

        } else if (gd.captcha_del == 0) {

            Msg lastMsg = msg.send(user.userName() + " 欢迎加入 ~").html().send();

			gd.last_join_msg = lastMsg.messageId();

        } else {

            msg.send(user.userName() + " 欢迎加入 ~").html().exec();

        }

        if (gd.welcome == null) return;

        if (gd.del_welcome_msg != null) {

            if (gd.last_welcome_msg != null) {

                execute(new DeleteMessage(gd.id,gd.last_welcome_msg));

            }

            if (gd.last_welcome_msg_2 != null) {

                execute(new DeleteMessage(gd.id,gd.last_welcome_msg_2));

            }

        }

        if (gd.welcome == 0) {

            if ((!((Integer) 1).equals(gd.delete_service_msg) && auth != null && auth.serviceMsg != null)) {

                if (gd.del_welcome_msg == null) {

                    auth.serviceMsg.reply(gd.welcomeMessage).html().async();

                } else {

                    SendResponse resp = auth.serviceMsg.reply(gd.welcomeMessage).html().exec();

                    if (resp != null && resp.isOk()) {

                        gd.last_welcome_msg = resp.message().messageId();

                    }

                }

            } else {

                if (gd.del_welcome_msg == null) {

                    msg.send(user.userName() + " , " + gd.welcomeMessage).html().async();

                } else {

                    SendResponse resp = msg.send(user.userName() + " , " + gd.welcomeMessage).html().exec();

                    if (resp != null && resp.isOk()) {

                        gd.last_welcome_msg = resp.message().messageId();

                    }

                }

            }

        } else {

            String sticker = gd.welcomeSet.get(RandomUtil.randomInt(0,gd.welcomeSet.size()));

            if (!((Integer) 1).equals(gd.delete_service_msg) && auth != null && auth.serviceMsg != null) {

                if (gd.del_welcome_msg == null) {

                    execute(new SendSticker(gd.id,sticker).replyToMessageId(auth.serviceMsg.messageId()));

                    if (gd.welcome == 2) {

                        msg.send(gd.welcomeMessage).html().async();

                    }

                } else {

                    SendResponse resp = execute(new SendSticker(gd.id,sticker));

                    if (resp != null && resp.isOk()) {

                        gd.last_welcome_msg = resp.message().messageId();

                    }

                    if (gd.welcome == 2) {

                        resp = msg.send(gd.welcomeMessage).html().exec();

                        if (resp != null && resp.isOk()) {

                            gd.last_welcome_msg_2 = resp.message().messageId();

                        }

                    }

                }


            } else {

                if (gd.del_welcome_msg == null) {

                    if (gd.welcome == 2) {

                        msg.send(user.userName() + " , " + gd.welcomeMessage).html().async();

                        executeAsync(new SendSticker(gd.id,sticker));

                    }

                } else {

                    SendResponse resp = msg.send(user.userName() + " , " + gd.welcomeMessage).html().exec();

                    if (resp != null && resp.isOk()) {

                        gd.last_welcome_msg = resp.message().messageId();

                    }

                    resp = execute(new SendSticker(gd.id,sticker));

                    if (resp != null && resp.isOk()) {

                        gd.last_welcome_msg = resp.message().messageId();

                    }

                }


            }


        }

		gd.log(this,"#加群验证 #通过 " + (cause == null ? "" : " #" + cause),"用户 : " + user.format());


    }

    void failed(UserData user,Msg msg,AuthCache auth,GroupData gd,String cause) {

        failed(user,msg,auth,gd,false,cause);

    }

    void failed(UserData user,Msg msg,AuthCache auth,GroupData gd,boolean noRetey,String cause) {

        if (cache.containsKey(msg.chatId())) {

            AuthCache rdc = cache.get(msg.chatId()).remove(user.id);

            if (auth == null) auth = rdc;

        }

        if (auth != null) {

            if (auth.authMsg != null) auth.authMsg.delete();

            auth.task.cancel();

            if (auth.serviceMsg != null) {

                auth.serviceMsg.delete();

                auth.serviceMsg = null;

            }

        }
		
		GetChatMemberResponse target = execute(new GetChatMember(gd.id,user.id.intValue()));

		if (!target.isOk() || !target.chatMember().isMember()) {

			msg.delete();

			gd.waitForCaptcha.remove(user.id);

			if (gd.require_input == null) {

				msg.unrestrict();

			}

			if (!(msg instanceof Callback)) {

				clearGroupPoint(user);

			}

			return;

		}
		

        if (!noRetey && gd.ft_count != null && (gd.captchaFailed == null || !gd.captchaFailed.containsKey(user.id.toString()) || (gd.captchaFailed.get(user.id.toString()) <= gd.ft_count))) {

            if (gd.captchaFailed == null) {
                gd.captchaFailed = new HashMap<>();

                gd.captchaFailed.put(user.id.toString(),1);

            } else if (!gd.captchaFailed.containsKey(user.id.toString())) {

                gd.captchaFailed.put(user.id.toString(),1);

            } else {

                gd.captchaFailed.put(user.id.toString(),gd.captchaFailed.get(user.id.toString()) + 1);

            }

            startAuth(user,msg,gd,auth != null ? auth.code : null);

            return;

        }

        //	if (msg.message().leftChatMember() != null) {
        //	} else if (msg.message().newChatMembers() != null) {0

        gd.waitForCaptcha.remove(user.id);

        if (gd.fail_ban == null) {

            msg.kick(user.id);

        } else {

            msg.kick(user.id,true);

        }

        if (gd.captcha_del != null && gd.last_join_msg != null) {

            executeAsync(msg.update,deleteMessage(msg.chatId(),gd.last_join_msg));

            gd.last_join_msg = null;

        }

        if (gd.captcha_del == null) {

            msg.send(user.userName() + " 验证失败 已被" + (gd.fail_ban == null ? "移除" : "封锁")).html().failed();

        } else if (gd.captcha_del == 0) {

            Msg lastMsg = msg.send(user.userName() + " 验证失败 已被" + (gd.fail_ban == null ? "移除" : "封锁")).html().send();

			gd.last_join_msg = lastMsg.messageId();

        } else {

            msg.send(user.userName() + " 验证失败 已被" + (gd.fail_ban == null ? "移除" : "封锁")).html().exec();

        }

        if (gd.passive_msg != null && gd.passive_msg.containsKey(user.id.toString())) {

            int id = gd.passive_msg.remove(user.id.toString());

            execute(new DeleteMessage(msg.chatId(),id));

            if (gd.passive_msg.isEmpty()) gd.passive_msg = null;

        }

        if (gd.captchaFailed != null) {

            gd.captchaFailed.remove(user.id.toString());

            if (gd.captchaFailed.isEmpty()) gd.captchaFailed = null;

        }

        msg.delete();

		gd.log(this,"#加群验证 #未通过 " + (cause == null ? "" : " #" + cause),"用户 : " + user.format());

    }

}
