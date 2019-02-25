package io.kurumi.ntt.utils;

import cn.hutool.log.Log;
import cn.hutool.log.StaticLog;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import io.kurumi.ntt.db.UserData;

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

        StringBuilder log = new StringBuilder("收到来自 ").append(user.name()).append(" (").append(user.userName()).append(") ").append(" 从");

        if (update.message() != null) {
            
            switch (update.message().chat().type()) {

                    case Private : log.append("私聊");break;

                    case group : log.append("群组 「").append(update.message().chat().title()).append("」 ");break;
                    case supergroup : log.append("超级群组 「").append(update.message().chat().title()).append("」 ");break;

                
            }
            
            log.append(processMessage(user,update.message(),point));

        } else if (update.channelPost() != null) {
            
            log.append("频道文章 : ").append(processMessage(user,update.channelPost(),point));
            
        } else if(update.callbackQuery() != null) {
            
            log.append("回调 : ").append(update.callbackQuery().data());
            
        } else if(update.inlineQuery() != null) {
            
            log.append("内联请求 : ").append(update.inlineQuery().query());
            
        }
        
        BotLog.debug(log.toString());

    }
    
    private static String processMessage(UserData user,Message msg,boolean point) {
        
        StringBuilder log = new StringBuilder();
        
        if (point) {

            log.append("指针 (").append(user.point().getPoint()).append(") ");

        }
        
        if (msg.forwardFromMessageId() != null) {
            
            log.append("转发从 " + UserData.get(msg.forwardFrom()).formattedName()).append(" ");
            
        }
        
        if (msg.audio() != null) log.append("「语音消息」");
        
        if (msg.channelChatCreated() != null) log.append("「被邀请到频道」");
        
        if (msg.connectedWebsite() != null) log.append("「连接到网页 : ").append(msg.connectedWebsite()).append("」");
        
        if (msg.contact() != null) log.append("「名片」");
        
        if (msg.deleteChatPhoto() != null) log.append("「照片删除」");
        
        if (msg.document() != null) log.append("「文件").append(msg.document().fileName()).append("」");
        
        if (msg.groupChatCreated() != null) log.append("「被邀请到群组」");
        
        if (msg.game() != null) log.append("「游戏 : ").append(msg.game().title()).append("」");
        
        if (msg.leftChatMember() != null) log.append("「群成员退出 : ").append(UserData.get(msg.leftChatMember()).formattedName()).append("」");
        
        if (msg.location() != null) log.append("「位置信息 : ").append(msg.location().toString()).append("」");
        
        if (msg.newChatMembers() != null) log.append("「新成员 : ").append(UserData.get(msg.newChatMember()).formattedName()).append("」");
        
        if (msg.newChatTitle() != null) log.append("「新标题 : ").append(msg.newChatTitle()).append("」");
        
        if (msg.newChatPhoto() != null) log.append("「新图标」");
        
        if (msg.photo() != null) log.append("「照片」");
        
        if (msg.pinnedMessage() != null) log.append("「置顶消息 :").append(processMessage(user,msg.pinnedMessage(),false));
        
        if (msg.replyToMessage() != null) log.append("「回复给 : ").append(UserData.get(msg.replyToMessage().from()).formattedName()).append(" : ").append(processMessage(UserData.get(msg.replyToMessage().from()),msg.replyToMessage(),false)).append("」");
        
        if (msg.sticker() != null) log.append("「贴纸 : ").append(msg.sticker().emoji()).append(" 从 ").append(msg.sticker().setName()).append("」");
        
        if (msg.supergroupChatCreated() != null) log.append("「被邀请到超级群组」");
        
        if (msg.video() != null) log.append("「视频」");
        
        if (msg.videoNote() != null) log.append("「小视频」");
        
        if (msg.voice() != null) log.append("「语音消息」");
        
        if (msg.text() != null) log.append(" : ").append(msg.text());
        
        return log.toString();
        
    }
    
    public static void pointSeted(UserData user,String point) {
        
        BotLog.debug("已设置对用户" + user.name() + " (" + user.userName() + ") 的输入指针 : " + point);
        
    }

    public static void publish(String message) {

        //  BotConf.LOG_CHANNEL_ID

    }

}
