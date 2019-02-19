package io.kurumi.ntt.fragment;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.DeleteWebhook;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SetWebhook;
import io.kurumi.ntt.BotConf;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.Query;
import io.kurumi.ntt.utils.CData;
import io.kurumi.ntt.utils.ThreadPool;

import java.util.LinkedList;
import java.util.List;
import io.kurumi.ntt.utils.BotLog;

public abstract class BotFragment extends Fragment implements UpdatesListener {

    private TelegramBot bot;
    private LinkedList<Fragment> fragments = new LinkedList<>();
    private String token;

    {

        fragments.add(this);

    }

    public BotFragment() {
        origin = this;
    }

    @Override
    public TelegramBot bot() {
        return bot;
    }

    public void addFragment(Fragment fragment) {

        fragment.origin = this;
        fragments.add(fragment);

    }

    public abstract String botName();

    public boolean isLongPulling() {
        return false;
    }

    @Override
    public int process(List<Update> updates) {

        for (Update update : updates) {

            processAsync(update);

        }

        return CONFIRMED_UPDATES_ALL;

    }

    public void processAsync(final Update update) {

        ThreadPool.exec(new Runnable() {

                @Override
                public void run() {

                    process(update);

                }

            });

    }

    public void process(final Update update) {

        if (update.message() != null) {

            UserData user = UserData.get(update.message().from());

            boolean point = user.hasPoint();

            BotLog.process(user, update, point);

            if (point) {

                CData data = user.point();

                for (Fragment fragmnet : fragments) {

                    if (fragmnet.onPoiMsg(user, new Msg(fragmnet, update.message()), data)) {

                        return;

                    }

                }

                switch (update.message().chat().type()) {

                        case Private: {

                            for (Fragment fragmnet : fragments) {

                                if (fragmnet.onPoiPrivMsg(user, new Msg(fragmnet, update.message()), data)) {

                                    return;

                                }

                            }

                            break;

                        }

                        case group: {

                            for (Fragment fragmnet : fragments) {

                                if (fragmnet.onPoiGroupMsg(user, new Msg(fragmnet, update.message()), data, false)) {

                                    return;

                                }

                            }

                            break;

                        }

                        default: {

                            for (Fragment fragmnet : fragments) {

                                if (fragmnet.onPoiGroupMsg(user, new Msg(fragmnet, update.message()), data, true)) {

                                    return;

                                }

                            }

                        }


                }

            } else {

                for (Fragment fragmnet : fragments) {

                    if (fragmnet.onMsg(user, new Msg(fragmnet, update.message()))) {

                        return;

                    }

                }

                switch (update.message().chat().type()) {

                        case Private: {

                            for (Fragment fragmnet : fragments) {

                                if (fragmnet.onPrivMsg(user, new Msg(fragmnet, update.message()))) {

                                    return;

                                }

                            }

                            break;

                        }

                        case group: {

                            for (Fragment fragmnet : fragments) {

                                if (fragmnet.onGroupMsg(user, new Msg(fragmnet, update.message()), false)) {

                                    return;

                                }

                            }

                            break;

                        }

                        default: {

                            for (Fragment fragmnet : fragments) {

                                if (fragmnet.onGroupMsg(user, new Msg(fragmnet, update.message()), true)) {

                                    return;

                                }

                            }

                        }


                }

            }

        } else if (update.channelPost() != null) {

            UserData user = UserData.get(update.channelPost().from());

            for (Fragment fragmnet : fragments) {

                if (fragmnet.onChanPost(user, new Msg(fragmnet, update.channelPost()))) {

                    return;

                }

            }

        } else if (update.callbackQuery() != null) {

            UserData user = UserData.get(update.callbackQuery().from());

            boolean point = user.hasPoint();

            if (point) {

                CData data = user.point();

                for (Fragment fragmnet : fragments) {

                    if (fragmnet.onPoiCallback(user, new Callback(fragmnet, update.callbackQuery()), data)) {

                        return;

                    }

                }

            } else {

                for (Fragment fragmnet : fragments) {

                    if (fragmnet.onCallback(user, new Callback(fragmnet, update.callbackQuery()))) {

                        return;

                    }

                }

            }

        } else if (update.inlineQuery() != null) {

            UserData user = UserData.get(update.channelPost().from());

            for (Fragment fragmnet : fragments) {

                if (fragmnet.onQuery(user, new Query(fragmnet, update.inlineQuery()))) {

                    return;

                }

            }

        }
        
    }

    public void start() {

        token = BotConf.getBotToken(botName());

        if (token == null || !BotConf.verifyToken(token)) {

            token = BotConf.inputToken(botName());

        }

        bot = new TelegramBot.Builder(token).build();

        if (isLongPulling()) {

            bot.setUpdatesListener(this, new GetUpdates());

        } else {

            bot.execute(new SetWebhook().url("https://" + BotConf.SERVER_DOMAIN + "/" + token));

            TGWebHookF.bots.put(token, this);

        }

    }

    public void stop() {

        if (isLongPulling()) {

            bot.removeGetUpdatesListener();

        } else {

            if (token != null) {

                TGWebHookF.bots.remove(token);

                bot.execute(new DeleteWebhook());

            }

        }

    }

}
