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
import java.util.*;

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


        if (data.useServer && Constants.authandwebhook.initServer(data.serverPort, data.serverDomain)) {

            log.info("服务器启动成功..");

            bot.setUpdatesListener(adapter, new GetUpdates().allowedUpdates(allows));
            
            bot.execute(new DeleteWebhook());
           
          //  bot.execute(new SetWebhook().url("https://" + data.serverDomain + "/" + data.botToken).allowedUpdates(allows), cb);

            try {

                System.in.read();

                System.out.println("正在停止BOT..");

                Constants.authandwebhook.server.stop();
                
                bot.removeGetUpdatesListener();
                
                BotControl.stopAll();

            } catch (IOException e) {}
          
        } else {

            log.error("服务器启动失败...");

        }

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
