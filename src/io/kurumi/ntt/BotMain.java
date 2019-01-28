package io.kurumi.ntt;

import cn.hutool.core.lang.caller.*;
import cn.hutool.log.*;
import com.pengrad.telegrambot.*;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.*;
import io.kurumi.ntt.bots.*;
import io.kurumi.ntt.ui.*;
import io.kurumi.ntt.webhookandauth.*;
import java.io.*;
import java.lang.reflect.*;

public class BotMain {

    public static final String version = "0.2";

    public Log log;

    public TelegramBot bot;
    public MainAdapter adapter;

    public Data data;

    public BotMain(File rootDir) {

        log = StaticLog.get("NTTBot");

        log.info("NTTBot 正在启动 版本 : " + version);

        //   Runtime.getRuntime().addShutdownHook(new Thread(new SaveData()));

        Constants.data = this.data = new Data(rootDir);

        data.doClean();

        adapter = new MainAdapter(this);

        if (data.botToken == null) {

            Setup.start();

        }

        Constants.bot = bot = new TelegramBot(data.botToken);

        bot.execute(new DeleteWebhook());

        Constants.authandwebhook = new ServerManager();

        final String[] allows = new String[] {
            TelegramUserBot.UPDATE_TYPE_MESSAGE,
            TelegramUserBot.UPDATE_TYPE_CALLBACK_QUERY
        };


        if (data.useServer) {

            log.info("正在启动认证和消息回调服务器...");

            if (Constants.authandwebhook.initServer(data.serverPort, data.serverDomain)) {

                log.info("服务器启动成功..");

                bot.execute(new GetMe(), new Callback<GetMe,GetMeResponse>() {

                        @Override
                        public void onResponse(GetMe req, GetMeResponse resp) {

                            Constants.thisUser = resp.user();

                            log.info("初始化成功");

                            Callback cb = new Callback<SetWebhook,BaseResponse>() {

                                @Override
                                public void onResponse(SetWebhook p1, BaseResponse resp) {

                                    if (!resp.isOk()) {

                                        System.err.println(resp.errorCode() + " : " + resp.description());
                                        System.exit(1);

                                    }

                                }

                                @Override
                                public void onFailure(SetWebhook p1, IOException ex) {
                                    ex.printStackTrace();
                                    System.exit(1);
                                }

                            };

                            bot.execute(new SetWebhook().url("https://" + data.serverDomain + "/" + data.botToken).allowedUpdates(allows), cb);

                            log.info("启动完成");

                        }

                        @Override
                        public void onFailure(GetMe req, IOException ex) {

                            log.error(ex, "初始化失败，请检查网络...");

                            log.info("正在尝试重启");

                            main(null);
                        }

                    });



            } else {

                log.error("服务器启动失败...");

            }


        } else {

            log.info("未设置认证和消息服务器 将使用用户发回URL的认证方法和GetUpdates读取消息...");

            bot.execute(new GetMe(), new Callback<GetMe,GetMeResponse>() {

                    @Override
                    public void onResponse(GetMe req, GetMeResponse resp) {

                        Constants.thisUser = resp.user();

                        log.info("初始化成功");


                        bot.setUpdatesListener(adapter, new GetUpdates().allowedUpdates(allows));

                        log.info("启动完成");

                    }

                    @Override
                    public void onFailure(GetMe req, IOException ex) {

                        log.error(ex, "初始化失败，请检查网络...");

                        log.info("正在尝试重启");

                        main(null);
                    }

                });

        }
        
    }

    public void stop() {

        bot.removeGetUpdatesListener();

    }

    public static void main(String[] args) {

        File rootDir = new File("/sdcard/AppProjects/NTTools");

        try {

            Class.forName("android.os.Build");

            try {

                Field caller = CallerUtil.class.getDeclaredField("INSTANCE");

                caller.setAccessible(true);

                caller.set(null, new StackTraceCaller());

            } catch (Exception e) {

                e.printStackTrace();

            }

        } catch (ClassNotFoundException ex) {  

            rootDir = new File(".");

        }

        new BotMain(rootDir);

    }

}
