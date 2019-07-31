package io.kurumi.ntt.fragment.bots;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.AbstractSend;
import io.kurumi.ntt.model.request.ButtonLine;
import io.kurumi.ntt.model.request.ButtonMarkup;

import java.util.LinkedList;

import io.kurumi.ntt.db.PointData;

public class MyBots extends Fragment {

    final String POINT_CHOOSE_BOT = "bot.c";
    final String POINT_BACK_TO_LIST = "bot.b";
    final String POINT_DELETE_BOT = "bot.d";
    final String POINT_CONFIRM_DEL = "bot.d.c";
    final String POINT_CHAT_BOT_EDIT_MESSAGE = "bot.c.e";

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerFunction("mybots");

        registerPoints(
                POINT_CHOOSE_BOT,
                POINT_BACK_TO_LIST,
                POINT_DELETE_BOT,
                POINT_CONFIRM_DEL,
                POINT_CHAT_BOT_EDIT_MESSAGE);


    }

    @Override
    public void onFunction(final UserData user, Msg msg, String function, String[] params) {

        showBotList(user, msg, false);

    }

    @Override
    public void onCallback(UserData user, Callback callback, String point, String[] params) {

        if (POINT_CHOOSE_BOT.equals(point)) {

            long botId = Long.parseLong(params[0]);

            showBot(true, user, callback, botId);

        } else if (POINT_BACK_TO_LIST.equals(point)) {

            showBotList(user, callback, true);

            callback.confirm();

        } else if (POINT_DELETE_BOT.equals(point)) {

            long botId = Long.parseLong(params[0]);

            deleteBot(user, callback, botId);

        } else if (POINT_CONFIRM_DEL.equals(point)) {

            long botId = Long.parseLong(params[0]);

            confirmDelete(user, callback, botId);

        } else if (POINT_CHAT_BOT_EDIT_MESSAGE.equals(point)) {

            long botId = Long.parseLong(params[0]);

            chatBotEditMessage(user, callback, botId);

        }

    }

    @Override
    public void onPoint(UserData user, Msg msg, String point, PointData data) {

        if (point.equals(POINT_CHAT_BOT_EDIT_MESSAGE)) {

            editChatBotMessage(user, msg, (BotEdit) data);

        }

    }

    void showBotList(final UserData user, Msg msg, boolean edit) {

        if (UserBot.data.countByField("user", user.id) == 0) {

            msg.sendOrEdit(edit, "你还没有任何BOT ，使用 /newbot 创建一只新BOT ~").async();

            return;

        }

        msg.sendOrEdit(edit, "从下方按钮中选择你的BOT :")
                .buttons(new ButtonMarkup() {{

                    ButtonLine line = null;

                    for (UserBot bot : UserBot.data.findByField("user", user.id)) {

                        if (line == null) {

                            line = newButtonLine();
                            line.newButton("@" + bot.userName, POINT_CHOOSE_BOT, bot.id);

                        } else {

                            line.newButton("@" + bot.userName, POINT_CHOOSE_BOT, bot.id);
                            line = null;

                        }

                    }

                }}).async();

    }

    void showBot(boolean edit, UserData user, Msg msg, long botId) {

        final UserBot bot = UserBot.data.getById(botId);

        if (bot == null || !bot.user.equals(user.id)) {

            if (msg instanceof Callback) {

                ((Callback) msg).alert("这个BOT无效");

            } else {

                msg.send("这个BOT无效...").async();

            }

            showBotList(user, msg, true);

            return;

        }

        AbstractSend send = msg.sendOrEdit(edit, "自定义" + bot.typeName() + " : @" + bot.userName, "", bot.information());
        send.buttons(new ButtonMarkup() {{

            if (bot.type == 0) {

                newButtonLine("更改欢迎语", POINT_CHAT_BOT_EDIT_MESSAGE, bot.id);

            }

            newButtonLine()
                    .newButton("删除BOT", POINT_DELETE_BOT, bot.id)
                    .newButton("返回列表", POINT_BACK_TO_LIST);

        }}).async();

    }

    void deleteBot(UserData user, Callback callback, long botId) {

        final UserBot bot = UserBot.data.getById(botId);

        if (bot == null || !bot.user.equals(user.id)) {

            callback.alert("这个BOT无效");

            showBotList(user, callback, true);

            return;

        }

        callback
                .edit("确认要删除 @" + bot.userName + " 吗？你会失去这只BOT，真的很久")
                .buttons(new ButtonMarkup() {{

                    newButtonLine("不删了", POINT_CHOOSE_BOT, bot.id);
                    newButtonLine("手滑了", POINT_CHOOSE_BOT, bot.id);
                    newButtonLine("点着玩", POINT_CHOOSE_BOT, bot.id);
                    newButtonLine("删掉罢", POINT_CONFIRM_DEL, bot.id);

                }}).async();

    }

    void confirmDelete(UserData user, Callback callback, long botId) {

        final UserBot bot = UserBot.data.getById(botId);

        if (bot == null || !bot.user.equals(user.id)) {

            callback.alert("这个BOT无效");

            showBotList(user, callback, true);

            return;

        }

        bot.stopBot();

        UserBot.data.deleteById(bot.id);

        showBotList(user, callback, true);

        callback.alert("已删除 @" + bot.userName);

    }

    void chatBotEditMessage(UserData user, Callback callback, long botId) {

        final UserBot bot = UserBot.data.getById(botId);

        if (bot == null || !bot.user.equals(user.id)) {

            callback.alert("这个BOT无效");

            showBotList(user, callback, true);

            return;

        }

        callback.confirm();

        BotEdit point = new BotEdit();

        point.bot = bot;
        point.context.add(callback);

        callback.edit("好,现在发送新的欢迎语 :").withCancel().async();

        setPrivatePoint(user, POINT_CHAT_BOT_EDIT_MESSAGE, point);

    }

    void editChatBotMessage(UserData user, Msg msg, BotEdit data) {

        data.context.add(msg);

        if (!msg.hasText()) {

            msg.send("你正在设置 @" + data.bot.userName + " 的欢迎语 ，请输入 : ").withCancel().exec(data);

            return;

        }

        clearPrivatePoint(user);

        data.bot.params.put("msg", msg.text());

        UserBot.data.setById(data.bot.id, data.bot);

        data.bot.reloadBot();

        msg.send("修改成功！").failed();

        showBot(false, user, msg, data.bot.id);

    }

    static class BotEdit extends PointData {

        UserBot bot;

    }


}
