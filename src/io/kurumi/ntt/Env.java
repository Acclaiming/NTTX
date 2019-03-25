package io.kurumi.ntt;

import cn.hutool.core.io.*;
import cn.hutool.json.*;
import com.pengrad.telegrambot.*;
import com.pengrad.telegrambot.request.*;
import io.kurumi.ntt.utils.*;
import java.io.*;
import java.util.*;

public class Env {

    /**
     * 创始人ID
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
