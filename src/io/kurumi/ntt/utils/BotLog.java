package io.kurumi.ntt.utils;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.log.Log;
import cn.hutool.log.dialect.console.ConsoleLog;
import cn.hutool.log.level.Level;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.fragment.abs.request.Send;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.LinkedHashSet;
import com.pengrad.telegrambot.request.ExportChatInviteLink;
import com.pengrad.telegrambot.response.StringResponse;
import com.pengrad.telegrambot.model.Message;

public class BotLog extends ConsoleLog {

    public static Log log = new BotLog();
    public static PrintStream out = System.out;

    private static String logFormat = "[#{level}] : {msg}";

    public BotLog() {

        super("NTTBot");

    }

    public static void log(Throwable t,String template,Object... values) {

		String str = StrUtil.format(template,values);

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

    }

    public static void error(String message,Throwable err) {

        log.error(err,message);

    }

    public static void errorWithStack(String message) {

        log.error(new RuntimeException(),message);


    }

    public static String parseError(Throwable error) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        error.printStackTrace(new PrintWriter(out,true));

        return StrUtil.utf8Str(out);

    }

	public static LinkedHashSet<Long> exportFailed = new LinkedHashSet<>();

    public static void process(BotFragment fragment,UserData user,Update update) {

		if (update.message() != null) {

			Msg msg = new Msg(fragment,update.message());

			StringBuilder info = new StringBuilder();

			info.append("来自用户 : " + msg.from().userName()).append("\n[").append(Html.code(msg.from().id)).append("]");

			if (!msg.isPrivate()) {

				info.append("\n来自群组 : ").append(HtmlUtil.escape(msg.chat().title())).append("\n[").append(Html.code(msg.chat().id())).append("]");

				if (msg.chat().username() != null) {
					
					info.append("\n群组身份 : @").append(msg.chat().username());
					
				}
				
				String link = msg.chat().inviteLink();

				if (!exportFailed.contains(msg.chatId() + msg.fragment.origin.me.id())) {

					StringResponse export = msg.fragment.bot().execute(new ExportChatInviteLink(msg.chatId()));

					if (export.isOk()) {

						link = export.result();

					} else {

						exportFailed.add(msg.fragment.origin.me.id() + msg.chatId());

					}

				}

				if (link != null) {

					info.append("\n邀请链接 : " + link);

				}

			}

			Message message = msg.message();

			if (message.newChatMember() != null) {
			
				UserData newData = UserData.get(message.newChatMember());
				
				info = new StringBuilder("群组新成员 : ").append(newData.userName()).append("\n[").append(Html.code(newData.id)).append(info);
				
			} 
			
			new Send(Env.LOG,info.toString()).html().exec();
			
			msg.forwardTo(Env.LOG);
		
		}

	}

    public static String formatName(User u) {

        if (u == null) return "匿名用户";

        return UserData.get(u).userName();

    }

    @Override
    public void log(Level level,Throwable t,String format,Object[] arguments) {

        if (false == isEnabled(level)) {
            return;
        }

        final Dict dict = Dict.create()
			.set("level",level.toString())
			.set("msg",StrUtil.format(format,arguments));

        final String logMsg = StrUtil.format(logFormat,dict);

		//7 this.log(t, logMsg);

		if (level == Level.DEBUG) {

			new Send(Env.LOG,logMsg).disableNotification().html().exec();

		} else {

			new Send(Env.LOG,logMsg).html().exec();

		}

		if (t != null) {

			new Send(Env.LOG,parseError(t)).exec();

		}

    }

}
