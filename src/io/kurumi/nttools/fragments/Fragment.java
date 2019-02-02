package io.kurumi.nttools.fragments;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.log.StaticLog;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Document;
import com.pengrad.telegrambot.model.InlineQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.DeleteWebhook;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetWebhook;
import io.kurumi.nttools.model.Callback;
import io.kurumi.nttools.model.Msg;
import io.kurumi.nttools.server.BotServer;
import io.kurumi.nttools.twitter.TwiAccount;
import io.kurumi.nttools.utils.CData;
import io.kurumi.nttools.utils.UserData;
import java.io.File;
import java.util.List;
import java.util.LinkedList;

public abstract class Fragment extends FragmentBase {

    public MainFragment main;
    public TelegramBot bot;
    public LinkedList<FragmentBase> fragments = new LinkedList<>();

    public Fragment(MainFragment main) {

        this.main = main;

        fragments.add(this);

    }

    public abstract String name();

    public Fragment initBot() {

        this.bot = new TelegramBot(main.tokens.get(name()));

        return this;

    }

    public void startGetUpdates() {

        deleteWebHook();

        bot.setUpdatesListener(new UpdatesListener() {

                @Override
                public int process(List<Update> updates) {

                    for (Update update : updates) {

                        processUpdate(update);

                    }

                    return CONFIRMED_UPDATES_ALL;
                }
            });

    }

    public void setWebHook() {

        BotServer.bots.put(main.tokens.get(name()), this);

        bot.execute(new SetWebhook().url("https://" + main.serverDomain + "/" + main.tokens.get(name())));

    }

    public void deleteWebHook() {

        bot.execute(new DeleteWebhook());

        BotServer.bots.remove(main.tokens.get(name()));

    }


    private UserData getUserData(Message msg) {

        UserData ud = main.getUserData(msg.from().id());
        ud.update(this, msg);
        return ud;

    }

    public void processUpdate(Update update) {

        UserData user = null;

        try {

        if (update.message() != null) {

        user = getUserData(update.message());

        switch (update.message().chat().type()) {

        case Private : {

        for (FragmentBase fragment : fragments) {

        fragment.processPrivateMessage(user, new Msg(this, update.message()));

        }

        return;

        }

        case group : {

        for (FragmentBase fragment : fragments) {

        fragment.processGroupMessage(user, new Msg(this, update.message()));

        }

        return;

        }

        case supergroup : {

        for (FragmentBase fragment : fragments) {

        fragment.processGroupMessage(user, new Msg(this, update.message()));

        }

        return;

        }

        }

        } else if (update.channelPost() != null) {



        user = getUserData(update.channelPost());

        for (FragmentBase fragment : fragments) {
        
        fragment. processChannelPost(user, new Msg(this, update.channelPost()));

        }

        } else if (update.callbackQuery() != null) {

        user = main.getUserData(update.callbackQuery().from());

        processCallbackQuery(user, new Callback(this, update.callbackQuery()));

        } else if (update.inlineQuery() != null) {

        user = main.getUserData(update.inlineQuery().from());

        processInlineQuery(user, update.inlineQuery());

        } else if (update.chosenInlineResult() != null) {

        user = main.getUserData(update.chosenInlineResult().from());

        processChosenInlineQueryResult(user, update.inlineQuery());

        }

        } catch (Exceptione) {

            StaticLog.error(e, "处理更新失败");


            StringBuilder err = new StringBuilder();

            err.append("Bot出错 : ");

            err.append("\n更新 : " + update);

            Throwable cause = e;

            while (cause != null) {

                err.append("\n\n错误 : " + cause.getClass().getName());

                err.append("\n\n" + cause.getMessage());

                for (StackTraceElement stack : cause.getStackTrace())  {

                    err.append("\nat : " + stack.toString());
                }

                cause = cause.getCause();

            }

            bot.execute(new SendMessage(user.id, err.toString()));




        }

    }

}
