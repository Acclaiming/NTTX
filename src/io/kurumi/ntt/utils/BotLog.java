package io.kurumi.ntt.utils;

import redis.clients.jedis.JedisPubSub;
import cn.hutool.log.StaticLog;
import io.kurumi.ntt.BotMain;
import io.kurumi.ntt.BotConf;
import cn.hutool.log.Log;

public class BotLog {
    
    public static Log log = StaticLog.get("NTTBot");
    
    public static void debug(String message) {
        
        log.warn(message);
        
    }
    
    public static void info(String message) {
        
        log.info(message);
        
    }
    
    public static void info(String message,Throwable err) {

        log.info(err,message);

    }
    
    public static void infoWithStack(String message) {

        log.info(new RuntimeException(),message);

    }
    
    public static void warn(String message) {

        log.info(message);

    }

    public static void warn(String message,Throwable err) {

        log.warn(err,message);

    }

    public static void warnWithStack(String message) {

        log.warn(new RuntimeException(),message);

    }
    
    public static void error(String message) {

        log.error(message);

    }

    public static void error(String message,Throwable err) {

        log.error(err,message);

    }

    public static void errorWithStack(String message) {

        log.error(new RuntimeException(),message);

    }
    
    public static void publish(String message) {
        
      //  BotConf.LOG_CHANNEL_ID
        
    }
    
}
