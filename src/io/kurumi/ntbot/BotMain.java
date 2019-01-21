package io.kurumi.ntbot;

import cn.hutool.core.lang.caller.*;
import cn.hutool.log.*;
import com.pengrad.telegrambot.*;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.*;
import io.kurumi.ntbot.auth.*;
import io.kurumi.ntbot.ui.*;
import io.kurumi.ntbot.ui.ext.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

public class BotMain {

    public static final String version = "0.1 build 1";

    public Log log;

    public TelegramBot bot;
    public MainAdapter adapter;

    public Data data;

    public BotMain(File rootDir) {

        log = StaticLog.get("NTTBot");

        log.info("NTTBot 正在启动 版本 : " + version);
        
        AuthManager auth = new AuthManager();

        log.info("正在启动OAuth认证服务器...");
        
        if (auth.init("ntt.kurumi.io")) {
            
            log.info("认证服务器启动成功..");
            
            } else {
            
            log.error("认证服务器启动失败...");
            log.error("将使用用户发回URL的认证方法...");
            
            
        }
        
        
        
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

                    log.info("初始化成功");

                    bot.setUpdatesListener(adapter);

                    log.info("启动完成");

                }

                @Override
                public void onFailure(GetMe req, IOException ex) {

                    log.error(ex,"初始化失败，请检查网络...");
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
            
            try {
                
                Field caller = CallerUtil.class.getDeclaredField("INSTANCE");

                caller.setAccessible(true);
                
                caller.set(null,new StackTraceCaller());
                
            } catch (Exception e) {
                
                e.printStackTrace();
                
            }

        } catch (ClassNotFoundException ex) {  

            rootDir = new File(".");

        }

        new BotMain(rootDir);

    }

}
