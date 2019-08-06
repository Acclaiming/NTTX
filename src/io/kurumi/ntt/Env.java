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

    public static String DB_ADDRESS;
    public static int DB_PORT;
    public static String SERVER_DOMAIN;

    public static File CACHE_DIR;
    public static File DATA_DIR;

    public static boolean USE_UNIX_SOCKET;
    public static File UDS_PATH;
    public static Integer LOCAL_PORT;

    public static String SERVICE_NAME;

    public static boolean DEBUG_MODE;
    public static String BOT_TOKEN;
	public static String[] ALIAS;
    public static long[] ADMINS;
    public static Long LOG_CHANNEL;
    public static Long TEP_CHANNEL;

    public static String HELP_MESSAGE;

	public static String NETEASE_COOKIE;
	
    private static JSONObject conf;

    public static void init() throws Exception {

        DATA_DIR = new File("/etc/ntt");

        conf = new JSONObject(FileUtil.readUtf8String(new File(DATA_DIR, "settings.json")));

        DB_ADDRESS = conf.getStr("db_address");


        DB_PORT = conf.getInt("db_port");
        SERVER_DOMAIN = conf.getStr("server_domain");

        SERVICE_NAME = conf.getStr("service_name");

        DEBUG_MODE = conf.getBool("debug_mode");
        USE_UNIX_SOCKET = conf.getBool("use_unix_socket");
        UDS_PATH = new File(conf.getStr("uds_path"));
        LOCAL_PORT = conf.getInt("local_port");

        CACHE_DIR = new File(conf.getStr("cache_path"));

        BOT_TOKEN = conf.getStr("bot_token");
		ALIAS = (String[]) conf.getJSONArray("alias").toArray(String.class);
        ADMINS = (long[]) conf.getJSONArray("admins").toArray(long.class);
        LOG_CHANNEL = conf.getLong("log_channel");
        TEP_CHANNEL = conf.getLong("tep_channel");

        HELP_MESSAGE = conf.getStr("help_message");

		NETEASE_COOKIE = conf.getStr("netease_cookie");
		
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
