package io.kurumi.ntt;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.caller.CallerUtil;
import cn.hutool.core.lang.caller.StackTraceCaller;
import cn.hutool.json.JSONObject;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.GetMe;
import io.kurumi.ntt.utils.BotLog;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Scanner;

public class Env {

	public static long[] ADMINS = new long[] {

		865266732 , 896711046,
		
		589593327 ,
		
		748525422 ,
		
		695492482

    };
	
    public static final Long GROUP = -1001428880645L;
		
    /**
     * 缓存文件存放地址
     */

    public static File CACHE_DIR;

    /**
     * 数据文件存放地址
     */

    public static File DATA_DIR;
    private static JSONObject conf = new JSONObject();

    static {

        // ROOT = new File("/etc/ntt");
        DATA_DIR = new File("/etc/ntt");
        CACHE_DIR = new File("/var/cache/ntt");

    }

    static {

        try {

            conf = new JSONObject(FileUtil.readUtf8String(new File(DATA_DIR, "settings.json")));

        } catch (Exception e) {
        }

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

            token = session.nextLine();

        }

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
