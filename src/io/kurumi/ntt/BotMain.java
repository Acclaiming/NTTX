package io.kurumi.ntt;

import cn.hutool.core.lang.caller.*;
import cn.hutool.log.*;
import com.pengrad.telegrambot.*;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.*;
import io.kurumi.ntt.ui.*;
import io.kurumi.ntt.webhookandauth.*;
import java.io.*;
import java.lang.reflect.*;

public class BotMain {

    public static final String version = "0.1 build 5";

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

        Constants.auth = new ServerManager();

        if (data.useServer) {

            log.info("正在启动OAuth认证服务器...");

            if (Constants.auth.initServer(data.serverPort, data.serverDomain)) {

                log.info("认证服务器启动成功..");

            } else {

                log.error("认证服务器启动失败...");
                log.error("将使用用户发回URL的认证方法...");

            }

        } else {

            log.info("未设置认证服务器 将使用用户发回URL的认证方法...");

        }

        bot.execute(new GetMe(), new Callback<GetMe,GetMeResponse>() {

                @Override
                public void onResponse(GetMe req, GetMeResponse resp) {

                    Constants.thisUser = resp.user();

                    log.info("初始化成功");
                    
                    String[] allows = new String[] {
                        UserBot.UPDATE_TYPE_MESSAGE,
                        UserBot.UPDATE_TYPE_CALLBACK_QUERY
                    };

                    bot.setUpdatesListener(adapter, new GetUpdates().allowedUpdates(allows));

                    log.info("启动完成");

                }

                @Override
                public void onFailure(GetMe req, IOException ex) {

                    log.error(ex, "初始化失败，请检查网络...");

                    log.info("正在常识重启");

                    main(null);
                }

            });



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
