package io.kurumi.ntt.utils;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.log.Log;
import cn.hutool.log.dialect.console.ConsoleLog;
import cn.hutool.log.level.Level;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.ExportChatInviteLink;
import com.pengrad.telegrambot.response.SendResponse;
import com.pengrad.telegrambot.response.StringResponse;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.Send;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashSet;

public class BotLog extends ConsoleLog {

    public static Log log = new BotLog();
    public static PrintStream out = System.out;

    private static String logFormat = "[#{level}] : {msg}";

    public BotLog() {

        super("NTTBot");

    }

    public static void log(Throwable t,String template,Object... values) {

		String str = StrUtil.format(template,values);

		//new Send(Env.GROUP,str).noLog().exec();

        out.println(str);

        if (null != t) {

			out.println(parseError(t));

        }

        out.flush();

    }

    public static void debug(String message) {

        log.debug(message);

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

        log.warn(message);

        //   new Send(Env.DEVELOPER_ID,"WARN : " + message).exec();

    }

    public static void warn(String message,Throwable err) {

        log.warn(err,message);

        //   sendToDeveloper(new Exception(message,err));

    }

    public static void warnWithStack(String message) {

        log.warn(new RuntimeException(),message);

    }

    public static void error(String message) {

        log.error(message);
		
		new Send(Env.LOG_CHANNEL, message).exec();
		
    }

    public static void error(String message,Throwable err) {

        log.error(err,message);

		new Send(Env.LOG_CHANNEL, message,parseError(err)).exec();

    }

    public static void errorWithStack(String message) {

		log.error(new RuntimeException(),message);

    }

    public static String parseError(Throwable error) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        error.printStackTrace(new PrintWriter(out,true));

        return StrUtil.utf8Str(out);

    }

	public static HashSet<Long> exportFailed = new HashSet<>();


    public static String formatName(User u) {

        if (u == null) return "匿名用户";

        return UserData.get(u).userName();

    }
	
}
