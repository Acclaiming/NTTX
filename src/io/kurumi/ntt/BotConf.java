package io.kurumi.ntt;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.log.StaticLog;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.GetMe;
import io.kurumi.ntt.db.BotDB;
import io.kurumi.ntt.utils.BotLog;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class BotConf {

    /**
     * 创始人ID (Disc/Telegram/Twitter)
     */

    public static final String FOUNDER = "HiedaNaKan";


    /**
     * 缓存文件存放地址
     */

    public static final File CACHE_DIR = new File("./cache");

    /**
     * 数据文件存放地址
     */

    public static final File DATA_DIR = new File("./data");

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
     * Discourse API调用地址 见 项目/disc/api.php
     */
    public static final String DISC_WAPPER = "http://127.0.0.1:11213/api.php";

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

        set("token." + name, token);

        return token;

    }

    /**
     * 检查 Token 是否可用
     */
    public static boolean verifyToken(String token) {

        return new TelegramBot(token).execute(new GetMe()).isOk();

    }

    private static JSONObject conf = new JSONObject(); static {

        try {

            conf = new JSONObject(FileUtil.readUtf8String(new File(DATA_DIR, "settings.json")));

        } catch (Exception e) {}

    }

    public static String get(String key) {

        if (key == null) {

            BotLog.warnWithStack("取设置值但键值为空");

            return null;

        }

        if (key.contains(".")) {

            return conf.getByPath(key, String.class);

        } else {

            return conf.getStr(key);

        }

    }

    public static String getOrDefault(String key, String defaultValue) {

        if (key == null) {

            BotLog.warnWithStack("取设置值或默认值但键值为空");

            return null;

        }


        String value = get(key);

        if (defaultValue == null) {

            BotLog.warnWithStack("取设置值或默认值但默认值为空 而不直接取值 : " + key);

        } else if (value == null) {

            set(key, defaultValue);

            value = defaultValue;

        }

        return value;

    }

    public static void set(String key, Object value) {

        if (value != null) value = value.toString();

        if (key.contains(".")) {

            conf.putByPath(key, value);

        } else {

            conf.put(key, value);

        }

        FileUtil.writeUtf8String(conf.toStringPretty(), new File(DATA_DIR, "settings.json"));

    }


    public static void cleanCache() {

        FileUtil.del(CACHE_DIR);

    }


}
