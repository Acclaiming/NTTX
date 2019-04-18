package io.kurumi.ntt.fragment;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.DeleteWebhook;
import com.pengrad.telegrambot.request.GetMe;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.response.GetMeResponse;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.Launcher;
import io.kurumi.ntt.db.BotDB;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.Query;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.BotLog;
import io.kurumi.ntt.utils.ThreadPool;
import java.util.LinkedList;
import java.util.List;
import okhttp3.OkHttpClient;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.*;

public abstract class BotFragment extends Fragment implements UpdatesListener {

	public User me;

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

	public void remFragment(Fragment fragment) {

        fragments.remove(fragment);


    }


    public abstract String botName();

    /*

     public boolean isLongPulling() {

     return false;

     }

     */

    @Override
    public int process(List<Update> updates) {

		for (final Update update : updates) {

			try {

				processAsync(update);

			} catch (Exception e) {

				BotLog.error("更新出错",e);

				Launcher.INSTANCE.uncaughtException(Thread.currentThread(),e);

			}


		}

		return CONFIRMED_UPDATES_ALL;

    }

    @Override
    public boolean onMsg(UserData user,Msg msg) {

        if ("cancel".equals(msg.command())) {

            msg.send("你要取消什么？ >_<").exec();

            return true;

        }

        return false;

    }

    @Override
    public boolean onPPM(UserData user,Msg msg) {

        if ("cancel".equals(msg.command())) {

            user.point = null;

            msg.send("取消成功 ~").exec();

            return true;

        }

        return false;

    }

    ExecutorService processUpdatePool = Executors.newFixedThreadPool(3);

    public void processAsync(final Update update) {

        final UserData user;

        if (update.message() != null) {

            user = BotDB.getUserData(update.message().from());

        } else if (update.channelPost() != null) {

            user = update.channelPost().from() != null ? BotDB.getUserData(update.channelPost().from()) : null;

        } else if (update.callbackQuery() != null) {

            user = BotDB.getUserData(update.callbackQuery().from());

        } else if (update.inlineQuery() != null) {

            user = BotDB.getUserData(update.inlineQuery().from());

        } else user = null;

        processUpdatePool.execute(new Runnable() {

                @Override
                public void run() {

                    if (update.message() != null) {

                        for (Fragment fragmnet : fragments) {

                            if (fragmnet.onUpdate(user,update)) {

                                return;

                            }

                        }

                        if (update.message().chat().type() == Chat.Type.Private && user != null && user.point != null) {

                            for (Fragment fragmnet : fragments) {

                                if (fragmnet.onPPM(user,new Msg(fragmnet,update.message()))) {



                                    return;

                                }

                            }

                            return;

                        }

                        for (Fragment fragmnet : fragments) {

                            if (fragmnet.onMsg(user,new Msg(fragmnet,update.message()))) {
                                return;

                            }

                        }

                        switch (update.message().chat().type()) {

                                case Private: {

                                    for (Fragment fragmnet : fragments) {

                                        if (fragmnet.onNPM(user,new Msg(fragmnet,update.message()))) {


                                            return;

                                        }

                                    }


                                    break;

                                }

                                case group: {

                                    for (Fragment fragmnet : fragments) {

                                        if (fragmnet.onGroupMsg(user,new Msg(fragmnet,update.message()),false)) {


                                            return;

                                        }

                                    }

                                    break;

                                }

                                default: {

                                    for (Fragment fragmnet : fragments) {

                                        if (fragmnet.onGroupMsg(user,new Msg(fragmnet,update.message()),true)) {

                                            return;

                                        }

                                    }

                                }

                        }

                    } else if (update.channelPost() != null) {



                        for (Fragment fragmnet : fragments) {

                            if (fragmnet.onUpdate(user,update)) {

                                return;

                            }

                        }


                        for (Fragment fragmnet : fragments) {

                            if (fragmnet.onChanPost(user,new Msg(fragmnet,update.channelPost()))) {

                                return;

                            }

                        }

                    } else if (update.callbackQuery() != null) {

                        for (Fragment fragmnet : fragments) {

                            if (fragmnet.onUpdate(user,update)) {

                                return;

                            }

                        }


                        for (Fragment fragmnet : fragments) {

                            if (fragmnet.onCallback(user,new Callback(fragmnet,update.callbackQuery()))) {

                                return;

                            }

                        }

                    } else if (update.inlineQuery() != null) {


                        for (Fragment fragmnet : fragments) {

                            if (fragmnet.onUpdate(user,update)) {

                                return;

                            }

                        }


                        for (Fragment fragmnet : fragments) {

                            if (fragmnet.onQuery(user,new Query(fragmnet,update.inlineQuery()))) {

                                return;

                            }

                        }

                    }


                }
            });

    }

    public boolean isLongPulling() {

        return false;

    }
    
    public String getToken() {
        
        return Env.get("token." + botName());
        
    }
    
    public void setToken(String botToken) {
        
        Env.set("token." + botName(),token);
         
    }

    public boolean silentStart() {

        token = getToken();

        bot = new TelegramBot.Builder(token).build();

        GetMeResponse resp = bot.execute(new GetMe());

        if (resp == null || !resp.isOk()) return false;

        me = resp.user();

        realStart();
        
        return true;

    }

    public void start() {

        token = getToken();

        if (token == null || !Env.verifyToken(token)) {

            token = Env.inputToken(botName());

        }
        
        setToken(token);

        OkHttpClient.Builder okhttpClient = new OkHttpClient.Builder();

        okhttpClient.networkInterceptors().clear();

        bot = new TelegramBot.Builder(token)
            .okHttpClient(okhttpClient.build()).build();

		me = bot.execute(new GetMe()).user();
        
        realStart();

    }
    
    public void realStart() {
        
        if (isLongPulling()) {

            bot.execute(new DeleteWebhook());
            bot.setUpdatesListener(this,new GetUpdates());

        } else {

            String url = "https://" + BotServer.INSTANCE.domain + "/" + token;
            
            BaseResponse resp = bot.execute(new SetWebhook().url(url));

            BotLog.debug("SET WebHook for " + botName() + " : " + url);
            
            if (!resp.isOk()) {
                
                BotLog.debug("Failed... : " + resp.description());
                
            } else {
                
                BotServer.fragments.put(token,this);
                
            }
            
            

        }
        
    }

    public void stop() {

        if (isLongPulling())  {

            bot.execute(new DeleteWebhook());

        } else {

            bot().removeGetUpdatesListener();

        }

        BotLog.info("BOT已停止 :)");

    }


}
