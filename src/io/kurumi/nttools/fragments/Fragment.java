package io.kurumi.nttools.fragments;

import cn.hutool.log.StaticLog;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.DeleteWebhook;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetWebhook;
import io.kurumi.nttools.model.Callback;
import io.kurumi.nttools.model.Msg;
import io.kurumi.nttools.model.request.Send;
import io.kurumi.nttools.server.BotServer;
import io.kurumi.nttools.utils.UserData;
import java.util.LinkedList;
import java.util.List;
import io.kurumi.nttools.model.request.AnswerCallback;

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

       // deleteWebHook();

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
    
    public void printUpdate(Update update) {
        
        StaticLog.debug(update.toString());
        
    }

    public void processUpdate(Update update) {

        UserData user = null;
        
       // printUpdate(update);

        try {

            if (update.message() != null) {

                user = main.getUserData(update.message().from());

                if ("/cancel".equals(update.message().text())) {

                    if (user.point != null) {

                        user.point = null;
                        user.save();

                        new Send(this, user.id, "已经取消输入 (｡>∀<｡)").exec();

                    } else {

                        new Send(this, user.id, "好像没有什么需要取消的 (｡>∀<｡)").exec();


                    }
                    
                    return;

                }

                switch (update.message().chat().type()) {

                        case Private : {

                            for (FragmentBase fragment : fragments) {

                                if (fragment.processPrivateMessage(user, new Msg(this, update.message()))) return;

                            }

                            return;

                        }

                        case group : {

                            for (FragmentBase fragment : fragments) {

                                if (fragment.processGroupMessage(user, new Msg(this, update.message()))) return;

                            }

                            return;

                        }

                        case supergroup : {

                            for (FragmentBase fragment : fragments) {

                                if (fragment.processGroupMessage(user, new Msg(this, update.message()))) return;

                            }

                            return;

                        }

                }

            } else if (update.channelPost() != null) {

                user = main.getUserData(update.channelPost().from());

                if ("/cancel".equals(update.message().text())) {

                    if (user.point != null) {

                        user.point = null;
                        user.save();

                        new Send(this, user.id, "已经取消输入 (｡>∀<｡)").exec();

                    } else {

                        new Send(this, user.id, "好像没有什么需要取消的 (｡>∀<｡)").exec();


                    }

                    return;

                }
                
                for (FragmentBase fragment : fragments) {

                    if (fragment. processChannelPost(user, new Msg(this, update.channelPost()))) return;

                }

            } else if (update.callbackQuery() != null) {

                user = main.getUserData(update.callbackQuery().from());

                if (user.point != null) {
                    
                    new AnswerCallback(this,update.callbackQuery().id()).alert("乃好像需要输入什么东西 (ﾟ⊿ﾟ)ﾂ \n\n取消输入使用 /cancel 哦！").exec();
                    
                    return;
                    
                }
                
                for (FragmentBase fragment : fragments) {

                    if (fragment.processCallbackQuery(user, new Callback(this, update.callbackQuery()))) return;

                }

            } else if (update.inlineQuery() != null) {

                user = main.getUserData(update.inlineQuery().from());

                for (FragmentBase fragment : fragments) {

                    if (fragment.processInlineQuery(user, update.inlineQuery())) return;

                }

            } else if (update.chosenInlineResult() != null) {

                user = main.getUserData(update.chosenInlineResult().from());

                for (FragmentBase fragment : fragments) {

                    if (fragment.processChosenInlineQueryResult(user, update.inlineQuery())) return;

                }

            }

        } catch (Exception e) {

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
