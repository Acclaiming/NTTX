package io.kurumi.ntbot;

import com.pengrad.telegrambot.*;
import io.kurumi.ntbot.ui.*;
import java.io.*;
import cn.hutool.log.*;
import java.util.*;
import com.pengrad.telegrambot.request.*;
import io.kurumi.ntbot.ui.ext.*;
import com.pengrad.telegrambot.response.*;

public class BotMain {

    public static final String version = "0.1 build 1";

    public Log log;

    public TelegramBot bot;
    public MainAdapter adapter;

    public Data data;

    public BotMain(File rootDir) {

        log = StaticLog.get("NTTBot");

        log.info("正在启动 版本 : " + version);

        this.data = new Data(rootDir);

        adapter = new MainAdapter(this);

        if ("".equals(data.getBotToken())) {

            log.error("未设置 BotToken");

            tryInputToken();

        }

        bot = new TelegramBot(data.getBotToken());

        Constants.bot = bot;
        Constants.data = data;

        bot.execute(new GetMe(), new Callback<GetMe,GetMeResponse>() {

                @Override
                public void onResponse(GetMe req, GetMeResponse resp) {

                    Constants.thisUser = resp.user();

                    log.info("初始化成功 /");

                    bot.setUpdatesListener(adapter);

                    log.info("启动完成");

                }

                @Override
                public void onFailure(GetMe req, IOException ex) {

                    log.error(ex);

                    log.error("初始化失败...");
                }

            });




    }

    public void tryInputToken() {

        Scanner session = new Scanner(System.in);

        System.out.println();
        System.out.print("输入BotToken : ");

        String token = session.next();

        System.out.print("确定吗？ y/N : ");

        String confirm = session.next();

        if ("n".equals(confirm.trim())) {

            tryInputToken();

            return;

        }

        data.setBotToken(token);
        data.save();

        log.debug("BotToken 已设置");

    }

    public void stop() {

        bot.removeGetUpdatesListener();

    }

    public static void main(String[] args) {

        File rootDir = new File("/sdcard/AppProjects/NTTools");

        try {

            Class.forName("android.os.Build");

        } catch (ClassNotFoundException ex) {  

            rootDir = new File(".");

        }

        new BotMain(rootDir);

    }

}
