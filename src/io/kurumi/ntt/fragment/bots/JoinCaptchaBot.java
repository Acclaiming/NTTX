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

public class JoinCaptchaBot extends BotFragment {

    static Timer timer = new Timer();
    final String POINT_AUTH = "auth";
    public Long botId;
    public Long userId;
    public String botToken;
    public String userName;
    public Long logChannel;
    public Boolean delJoin;
    HashMap<Long, HashMap<Long, Msg>> cache = new HashMap<>();

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
    public boolean onMsg(UserData user, final Msg msg) {

		if (user.developer()) return false;
		
        if (msg.message().groupChatCreated() != null || msg.message().supergroupChatCreated() != null) {

            msg.send("欢迎使用由 @NTT_X 驱动的开源加群验证BOT", "给BOT 删除消息 和 封禁用户 权限就可以使用了 ~").exec();

        } else if (msg.message().leftChatMember() != null) {

            if (cache.containsKey(msg.chatId().longValue())) {

                HashMap<Long, Msg> group = cache.get(msg.chatId().longValue());

                if (group.containsKey(user.id.longValue())) {

                    group.remove(user.id.longValue()).delete();

                    if (group.isEmpty()) cache.remove(msg.chatId().longValue());

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

                new Send(this, logChannel, "事件 : #成员退出", "群组 : " + msg.chat().title(), "[" + Html.code(msg.chatId().toString()) + "]", "用户 : " + left.userName(), "#id" + left.id).html().exec();

            }

        } else if (msg.message().newChatMember() != null || msg.message().newChatMembers() != null) {

            GetChatMemberResponse resp = bot().execute(new GetChatMember(msg.chatId(), me.id().intValue()));

            if (resp == null && !resp.isOk()) return true;

            if (!resp.chatMember().canDeleteMessages()) {

                msg.send("机器人没有 删除消息 权限，已退出 :(").exec();
                msg.exit();

                return true;

            }

            if (!resp.chatMember().canRestrictMembers()) {

                msg.send("机器人没有 封禁用户 权限，已退出 :(").exec();
                msg.exit();

                return true;

            }

            if (delJoin) msg.delete();

            final HashMap<Long, Msg> group = cache.containsKey(msg.chatId().longValue()) ? cache.get(msg.chatId()) : new HashMap<Long, Msg>();

            User newMember = msg.message().newChatMember();

            if (newMember == null) newMember = msg.message().newChatMembers()[0];

            if (newMember.isBot()) {

                if (newMember.id().equals(botId)) {

                    msg.send("欢迎使用由 @NTT_X 驱动的开源加群验证BOT", "给BOT 删除消息 和 封禁用户 权限就可以使用了 ~").exec();

                }

                return false;

            }

            final UserData newData = UserData.get(newMember);

            String[] info = new String[]{

                    "你好呀，新加裙的绒布球 " + newData.userName() + " ~\n",

                    "现在需要确认一下乃是不是机器人绒布球了 ~\n",

                    "发送 喵 就可以通过验证了 ~ 3分钟以内呀 (๑˃̵ᴗ˂̵)و \n",

                    "注意不要点按钮 喵 ~"

            };


            ButtonMarkup buttons = new ButtonMarkup() {{

                newButtonLine()
                        .newButton("不要", POINT_AUTH, newData.id)
                        .newButton("点", POINT_AUTH, newData.id)
                        .newButton("按钮", POINT_AUTH, newData.id)
                        .newButton("喵", POINT_AUTH, newData.id);

                newButtonLine()
                        .newButton("绒布", POINT_AUTH, newData.id)
                        .newButton("球", POINT_AUTH, newData.id)
                        .newButton("点", POINT_AUTH, newData.id)
                        .newButton("按钮", POINT_AUTH, newData.id);

                newButtonLine()
                        .newButton("可以", POINT_AUTH, newData.id)
                        .newButton("直接", POINT_AUTH, newData.id)
                        .newButton("滥权", POINT_AUTH, newData.id)
                        .newButton("喵", POINT_AUTH, newData.id);


            }};

            setPoint(newData, POINT_AUTH);

            group.put(newMember.id(), msg.send(info).buttons(buttons).html().send());

            cache.put(msg.chatId().longValue(), group);

            timer.schedule(new TimerTask() {

                @Override
                public void run() {

                    final HashMap<Long, Msg> group = cache.containsKey(msg.chatId().longValue()) ? cache.get(msg.chatId()) : new HashMap<Long, Msg>();

                    if (group.containsKey(newData.id.longValue())) {

                        clearPoint(newData);

                        group.remove(newData.id).delete();

                        if (group.isEmpty()) {

                            cache.remove(msg.chatId().longValue());

                        } else {

                            cache.put(msg.chatId().longValue(), group);

                        }

                        if (msg.kick(newData.id)) {

                            msg.send(newData.userName() + " 不理解喵喵的语言 , 真可惜喵...").html().failed(60 * 1000);

                            if (logChannel != null) {

                                new Send(origin, logChannel, "事件 : #未通过 #超时", "群组 : " + msg.chat().title(), "[" + Html.code(msg.chatId().toString()) + "]", "用户 : " + newData.userName(), "#id" + newData.id).html().exec();

                            }

                        }

                    }

                }

            }, new Date(System.currentTimeMillis() + 3 * 60 * 1000));

        } else if (msg.isPrivate()) {

            msg.send("喵....？").exec();

        }

        return false;

    }

    @Override
    public boolean onCallback(UserData user, Callback callback) {

        long target = Long.parseLong(callback.params[1]);

        if (user.id.equals(target)) {

            HashMap<Long, Msg> group = cache.containsKey(callback.chatId().longValue()) ? cache.get(callback.chatId()) : new HashMap<Long, Msg>();

            if (group.containsKey(user.id)) {

                group.remove(user.id).delete();

                if (group.isEmpty()) {

                    cache.remove(callback.chatId().longValue());

                } else {

                    cache.put(callback.chatId().longValue(), group);

                }

            }

            clearPoint(user);

            if (callback.kick(user.id)) {

                callback.send(user.userName() + " 瞎按按钮 , 未通过验证 , 真可惜喵...").html().failed(60 * 1000);

                if (logChannel != null) {

                    new Send(this, logChannel, "事件 : #未通过 #点击按钮", "群组 : " + callback.chat().title(), "[" + Html.code(callback.chatId().toString()) + "]", "用户 : " + user.userName(), "#id" + user.id).html().exec();

                }

            }

        } else if (NTT.isGroupAdmin(this, callback.chatId(), user.id)) {

            HashMap<Long, Msg> group = cache.containsKey(callback.chatId().longValue()) ? cache.get(callback.chatId()) : new HashMap<Long, Msg>();

            if (group.containsKey(target)) {

                group.remove(target).delete();

                if (group.isEmpty()) {

                    cache.remove(callback.chatId().longValue());

                } else {

                    cache.put(callback.chatId().longValue(), group);

                }

            }

            UserData t = UserData.get(target);

            clearPoint(t);

            if (callback.kick(target, true)) {

                callback.send(t.userName() + " 被绒布球滥权了 , 真可惜喵...").html().failed(15 * 1000);

                if (logChannel != null) {

                    new Send(this, logChannel, "事件 : #未通过 #管理员封禁", "群组 : " + callback.chat().title(), "[" + Html.code(callback.chatId().toString()) + "]", "用户 : " + t.userName(), "#id" + t.id).html().exec();

                }

            }


        } else {

            callback.alert("不要乱点按钮喵 ~");

        }

        return true;

    }

    @Override
    public boolean onPointedGroup(UserData user, Msg msg) {

        HashMap<Long, Msg> group = cache.containsKey(msg.chatId().longValue()) ? cache.get(msg.chatId()) : new HashMap<Long, Msg>();

        if (group.containsKey(user.id)) {

            group.remove(user.id).delete();

            if (group.isEmpty()) {

                cache.remove(msg.chatId().longValue());

            } else {

                cache.put(msg.chatId().longValue(), group);

            }

        }

        clearPoint(user);

        if (msg.hasText() && msg.text().contains("喵")) {

            msg.send(user.userName() + " 通过了图灵(划掉) 验证 ~").html().failed(15 * 1000);

            if (logChannel != null) {

                new Send(this, logChannel, "事件 : #通过验证", "群组 : " + msg.chat().title(), "[" + Html.code(msg.chatId().toString()) + "]", "用户 : " + user.userName(), "#id" + user.id).html().exec();

            }

        } else if (msg.kick()) {

            msg.delete();

            msg.send(user.userName() + " 不懂喵喵的语言 , 真可惜喵...").html().failed(60 * 1000);

            if (logChannel != null) {

                new Send(this, logChannel, "事件 : #未通过 #发送其他内容", "群组 : " + msg.chat().title(), "[" + Html.code(msg.chatId().toString()) + "]", "用户 : " + user.userName(), "#id" + user.id).html().exec();

            }

        }

        return true;

    }

}
