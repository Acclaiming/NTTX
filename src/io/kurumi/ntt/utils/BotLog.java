package io.kurumi.ntt.utils;

import cn.hutool.log.Log;
import cn.hutool.log.StaticLog;
import io.kurumi.ntt.db.UserData;
import com.pengrad.telegrambot.model.Update;

public class BotLog {

    public static Log log = StaticLog.get("NTTBot");

    public static void debug(String message) {

        log.debug(message);

    }

    public static void info(String message) {

        log.info(message);

    }

    public static void info(String message, Throwable err) {

        log.info(err, message);

    }

    public static void infoWithStack(String message) {

        log.info(new RuntimeException(), message);

    }

    public static void warn(String message) {

        log.warn(message);

    }

    public static void warn(String message, Throwable err) {

        log.warn(err, message);

    }

    public static void warnWithStack(String message) {

        log.warn(new RuntimeException(), message);

    }

    public static void error(String message) {

        log.error(message);

    }

    public static void error(String message, Throwable err) {

        log.error(err, message);

    }

    public static void errorWithStack(String message) {

        log.error(new RuntimeException(), message);

    }

    public static void process(UserData user, Update update, boolean point) {

        StringBuilder log = new StringBuilder("收到来自 ").append(user.name()).append(" (").append(user.userName()).append(") ").append(" 的");

        if (update.message() != null) {

            switch (update.message().chat().type()) {
                
                case Private : log.append("私聊");break;
                case group : log.append("群组");break;
                case supergroup : log.append("超级群组");break;
                
            }
            
            if (point) {

                log.append("指针 (").append(user.point().getPoint()).append(") ");

            }

            log.append("消息 : ").append(update.message().caption());

        } else if (update.channelPost() != null) {
            
            log.append("频道文章 : ").append(update.message().caption());
            
        } else if(update.callbackQuery() != null) {
            
            log.append("回调 : ").append(update.callbackQuery().data());
            
        } else if(update.inlineQuery() != null) {
            
            log.append("内联请求 : ").append(update.inlineQuery().query());
            
        }
        
        BotLog.debug(log.toString());

    }
    
    public static void pointSeted(UserData user,String point) {
        
        BotLog.debug("已设置对用户" + user.name() + " (" + user.userName() + ") 的输入指针 : " + point);
        
    }

    public static void publish(String message) {

        //  BotConf.LOG_CHANNEL_ID

    }

}
