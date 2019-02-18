package io.kurumi.ntt;

import cn.hutool.core.io.FileUtil;
import cn.hutool.log.StaticLog;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.GetMe;
import io.kurumi.ntt.db.BotDB;
import io.kurumi.ntt.utils.BotLog;

import java.io.File;
import java.util.Scanner;

public class BotConf {

    public static final String TOKEN_KEY = "NTT_TOKEN";
    /**
     * 创始人ID
     */

    public static final String FOUNDER = "HiedaNaKan";
    /**
     * 缓存文件存放地址
     */

    public static final File CACHE_DIR = new File("./cache");
    /**
     * Bot服务器的域名 注意 Bot不会绑定此域名
     * <p>
     * 而且是应该用Nginx等反向代理服务器代理该域名
     * <p>
     * 下面设置的本地端口
     * <p>
     * 注意 : 不应该带有 "http(s)://" 前缀 和 "/" 后缀
     */
    public static final String SERVER_DOMAIN = "ntt.kurumi.io";
    /**
     * 本地服务器端口 请将上方的域名反向代理到此端口
     */
    public static final int LOCAL_PORT = 18964;
    /**
     * Twitter API Key
     * <p>
     * 注意 : 应用回调地址必须为 https://[上方填写的域名]/callback
     */

    public static final String TWITTER_CONSUMER_KEY = "pLkoUI2q5ZncKKIm7dQNqtpXT";
    /**
     * Twutter Key Sec
     */
    public static final String TWITTER_CONSUMER_KEY_SEC = "Yp59pXMXoHKD8dj2g1m6RUc7VNIJybBHH1NtM70MhOB0OKl00S";
    /**
     * Bot 日志频道 不带@ (必须是公开频道 且Bot可访问)
     */
    public static final String LOG_CHANNEL = "NTTSpamPublic";
    public static final String LOG_CHANNEL_ID = "@" + LOG_CHANNEL;
    public static final String LOG_CHANNEL_URL = "https://" + LOG_CHANNEL + "/";
    /**
     * Redis 数据库主机
     */
    public static final String REDIS_HOST = "localhost";
    /**
     * Redis 数据库端口
     */
    public static final int REDIS_PORT = 6379;
    /**
     * Redis 数据库密码
     */
    public static final String REDIS_PSWD = null;
    /**
     * Redis NTTBot 数据库 默认 0
     */
    public static final int REDIS_DB = 0;
    /**
     * 设置
     */

    public static String PROP_KEY = "NTT_PROP";

    public static String getBotToken(String key) {

        return BotDB.jedis.hget(TOKEN_KEY, key);

    }

    public static void saveBotToken(String key, String token) {

        BotDB.jedis.hset(TOKEN_KEY, key, token);

    }

    /**
     * 命令行输入 Token 并保存到数据库
     */
    public static String inputToken(String name) {

        Scanner session = new Scanner(System.in);

        System.out.print("输入" + name + " BotToken : ");

        String token = session.next();

        while (!verifyToken(token)) {

            System.out.println();
            System.out.println("BotToken 无效 ！ ");
            System.out.print("重新输入" + name + "的 BotToken : ");

            token = session.next();

        }

        saveBotToken(name, token);

        return token;

    }

    /**
     * 检查 Token 是否可用
     */
    public static boolean verifyToken(String token) {

        return new TelegramBot(token).execute(new GetMe()).isOk();

    }

    public static String get(String key) {

        if (key == null) {

            BotLog.warnWithStack("取设置值但键值为空");

            return null;

        }

        return BotDB.jedis.hget(PROP_KEY, key);

    }

    public static String getOrDefault(String key, String defaultValue) {

        if (key == null) {

            BotLog.warnWithStack("取设置值或默认值但键值为空");

            return null;

        }


        String value = get(key);

        if (defaultValue == null) {

            BotLog.warnWithStack("取设置值或默认值但默认值为空 而不直接取值 : " + key);

        }

        return value == null ? defaultValue : value;

    }

    public static void set(String key, Object value) {

        if (value != null) {

            BotDB.jedis.hset(PROP_KEY, key, value.toString());

            return;

        }

        BotLog.warnWithStack("对数据库设置空值而不使用 HDEL 已忽略 : " + key);

    }

    public static void remove(String key) {

        if (key == null) {

            StaticLog.warn(new RuntimeException(), "移除设置值但键值为空");

            return;

        }

        BotDB.jedis.hdel(PROP_KEY, key);

    }

    public static void cleanCache() {

        FileUtil.del(CACHE_DIR);

    }


}
