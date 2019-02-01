package io.kurumi.nttools.model;

import cn.hutool.core.util.StrUtil;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.DeleteMessage;
import io.kurumi.nttools.fragments.Fragment;
import io.kurumi.nttools.model.request.Edit;
import io.kurumi.nttools.model.request.Send;
import io.kurumi.nttools.utils.UserData;

public class Msg extends Context {

    private Message message;

    public Msg(Fragment fragment, Message message) {
        
        super(fragment,message.chat());
        
        this.fragment = fragment;
        this.message = message;
        
    }
    
    public Message message() { return message; }

    public int messageId() { return message.messageId(); }

    public boolean hasText() {

        return message.text() != null;

    }

    public String text() {

        return message.text();

    }
    
    public Send reply(String... msg)  {

        return send(msg).replyTo(this);

    }
    
    public Edit edit()  {

        return new Edit(fragment,chatId(),messageId(),text());

    }
    
    public Edit edit(String... msg)  {
        
        return new Edit(fragment,chatId(),messageId(),msg);

    }
    
    public void delete() {
        
        fragment.bot.execute(new DeleteMessage(chatId(),messageId()));
        
    }
    
    public boolean isCommand() {

        if (text() == null) return false;

        return text().startsWith("/");

    }

    public String commandName() {

        if (text() == null) return null;

        if (!text().contains("/")) return null;

        String body = StrUtil.subAfter(text(), "/", false);

        if (body.contains(" ")) {

            String cmdAndUser =  StrUtil.subBefore(body, " ", false);

            if (cmdAndUser.contains("@")) {

                return StrUtil.subBefore(cmdAndUser, "@", false);

            } else {

                return cmdAndUser;

            }

        } else if (body.contains("@")) {

            return StrUtil.subBefore(body, "@", false);

        } else {

            return body;

        }

    }
    
    public static String[] NO_PARAMS = new String[0];

    public String[] commandParms() {

        if (text() == null) return NO_PARAMS;
        
        if (!text().contains("/")) return NO_PARAMS;

        String body = StrUtil.subAfter(text(), "/", false);

        if (body.contains(" ")) {

            return StrUtil.subAfter(body, " ", false).split(" ");

        } else {

            return NO_PARAMS;

        }

    }
    

}
