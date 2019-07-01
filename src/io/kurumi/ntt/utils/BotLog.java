package io.kurumi.ntt.utils;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.dialect.console.ConsoleLog;
import cn.hutool.log.level.Level;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.abs.request.Send;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

public class BotLog extends ConsoleLog {

    public static Log log = new BotLog();
    public static PrintStream out = System.out;

    private static String logFormat = "[{level}] : {msg}";

    public BotLog() {
		
        super("NTTBot");
		
    }

    public static void log(Throwable t, String template, Object... values) {

		String str = StrUtil.format(template, values);
		
        out.println();

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

    public static void info(String message, Throwable err) {

        log.info(err, message);

    }

    public static void infoWithStack(String message) {

        log.info(new RuntimeException(), message);

    }

    public static void warn(String message) {

        log.warn(message);

        //   new Send(Env.DEVELOPER_ID,"WARN : " + message).exec();

    }

    public static void warn(String message, Throwable err) {

        log.warn(err, message);

        //   sendToDeveloper(new Exception(message,err));

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

    public static String parseError(Throwable error) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        error.printStackTrace(new PrintWriter(out, true));

        return StrUtil.utf8Str(out);

    }

    public static void process(BotFragment fragment,UserData user, Update update) {

        StringBuilder log = new StringBuilder();

        if (update.message() != null) {

            log.append(UserData.get(fragment.me).userName()).append(" 收到来自 ").append(user.userName());

            switch (update.message().chat().type()) {

                case Private:
                    log.append("的私聊");
                    break;

                case group:
                    log.append("在群组 「").append(update.message().chat().title()).append("」 ");
                    break;
					
                case supergroup:
                    log.append("在超级群组 「").append(update.message().chat().title()).append("」 ");
                    break;


            }

            log.append("消息 :");

            log.append(processMessage(user, update.message()));

        } else if (update.channelPost() != null) {

            log.append("频道消息 : ").append(processMessage(user, update.channelPost()));

        } else if (update.callbackQuery() != null) {

            log.append("回调 : ").append(update.callbackQuery().data());

        } else if (update.inlineQuery() != null) {

            log.append("内联请求 : ").append(update.inlineQuery().query());

        }

        BotLog.debug(log.toString());

    }

    private static String processMessage(UserData user, Message msg) {

        StringBuilder log = new StringBuilder();

        if (msg.forwardFromChat() != null) {

            UserData ff = UserData.get(msg.forwardFrom());

            log.append(ff != null ? ff.formattedName() : "匿名用户").append(" ");

        }

        if (msg.audio() != null) log.append("「语音消息」");

        if (msg.channelChatCreated() != null) log.append("「被邀请到频道」");

        if (msg.connectedWebsite() != null) log.append("「连接到网页 : ").append(msg.connectedWebsite()).append("」");

        if (msg.contact() != null) log.append("「名片」");

        if (msg.deleteChatPhoto() != null) log.append("「照片删除」");

        if (msg.document() != null) log.append("「文件").append(msg.document().fileName()).append("」");

        if (msg.groupChatCreated() != null) log.append("「被邀请到群组」");

        if (msg.game() != null) log.append("「游戏 : ").append(msg.game().title()).append("」");

        if (msg.leftChatMember() != null)
            log.append("「群成员退出 : ").append(UserData.get(msg.leftChatMember()).formattedName()).append("」");

        if (msg.location() != null) log.append("「位置信息 : ").append(msg.location().toString()).append("」");

        if (msg.newChatMembers() != null)
            log.append("「新成员 : ").append(UserData.get(msg.newChatMembers()[0]).formattedName()).append("」");

        if (msg.newChatTitle() != null) log.append("「新标题 : ").append(msg.newChatTitle()).append("」");

        if (msg.newChatPhoto() != null) log.append("「新图标」");

        if (msg.photo() != null) log.append("「照片」");

        if (msg.pinnedMessage() != null) log.append("「置顶消息 :").append(processMessage(user, msg.pinnedMessage()));

        if (msg.replyToMessage() != null) {

            log.append("「回复给 : ").append(formatName(msg.replyToMessage().from())).append(" : ").append(processMessage(UserData.get(msg.replyToMessage().from()), msg.replyToMessage())).append("」");

        }

        if (msg.sticker() != null)
            log.append("「贴纸 : ").append(msg.sticker().emoji()).append(" 从 ").append(msg.sticker().setName()).append("」");

        if (msg.supergroupChatCreated() != null) log.append("「被邀请到超级群组」");

        if (msg.video() != null) log.append("「视频」");

        if (msg.videoNote() != null) log.append("「小视频」");

        if (msg.voice() != null) log.append("「语音消息」");

        if (msg.text() != null) log.append(" : ").append(msg.text());

        return log.toString();

    }

    public static String formatName(User u) {

        if (u == null) return "匿名用户";

        return UserData.get(u).formattedName();

    }

    public static void pointSeted(UserData user, String point) {

        BotLog.debug("已设置对用户" + user.name() + " (" + user.userName() + ") 的输入指针 : " + point);

    }

    @Override
    public void log(Level level, Throwable t, String format, Object[] arguments) {

        if (false == isEnabled(level)) {
            return;
        }

        final Dict dict = Dict.create()
                .set("level", level.toString())
                .set("msg", StrUtil.format(format, arguments));

        final String logMsg = StrUtil.format(logFormat, dict);

       //7 this.log(t, logMsg);

		if (level == Level.DEBUG) {
		
			new Send(Env.LOG,logMsg).disableNotification().exec();
			
		} else {
			
			new Send(Env.LOG,logMsg).exec();
			
		}
		
    }

}
