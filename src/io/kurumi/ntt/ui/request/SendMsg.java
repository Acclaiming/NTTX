package io.kurumi.ntt.ui.request;

import cn.hutool.core.util.*;
import com.pengrad.telegrambot.*;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.ui.*;
import io.kurumi.ntt.ui.model.*;
import java.util.*;
import java.io.*;
import cn.hutool.log.*;

public class SendMsg extends AbsSendMsg {

    private SendMessage send;

    public SendMsg(Chat chat, String... sendMsg) {

        send = new SendMessage(chat.id(), ArrayUtil.join(sendMsg, "\n"));

    }

    public SendMsg(Message msg, String... sendMsg) {

        this(msg.chat(), sendMsg);
        send.replyToMessageId(msg.messageId());

    }
    
    private int keyboaordType = 0;

    public SendMsg hideKeyboard() {

        keyboaordType = 1;

        return this;

    }

    public SendMsg removeKeyboard() {

        keyboaordType = 2;

        return this;

    }

    @Override
    public SendMsg html() {

        send.parseMode(ParseMode.HTML);

        return this;

    }

    @Override
    public AbsSendMsg disableWebPagePreview() {

        send.disableWebPagePreview(true);

        return this;

    }

    @Override
    public SendMsg disableNotification() {

        send.disableNotification(true);

        return this;

    }


    @Override
    public SendMsg markdown() {

        send.parseMode(ParseMode.Markdown);

        return this;

    }
    
    private LinkedList<ReplyButtonGroup> replyButtonGroups = new LinkedList<>();

    public SendMsg sinleLineReplyButton(String text) {
        
        newReplyButtonGroup().newButton(text);
        
        return this;
        
    }
    
    public SendMsg sinleLineReplyRequestLocationButton(String text) {

        newReplyButtonGroup().newRequestLocationButton(text);

        return this;

    }
    
    public SendMsg singleLineRequestContactButton(String text) {

        newReplyButtonGroup().newRequestContactButton(text);

        return this;

    }
    
    public ReplyButtonGroup newReplyButtonGroup() {
        
        ReplyButtonGroup group = new ReplyButtonGroup();

        replyButtonGroups.add(group);
        
        return group;
        
    }

    @Override
    public SendMsg singleLineOpenUrlButton(String text, String url) {
       
        super.singleLineOpenUrlButton(text, url);
        
        return this;
        
    }
  
    
    @Override
    public SendMsg singleLineButton(String text, DataObject obj) {

        super.singleLineButton(text, obj);

        return this;

    }

    @Override
    public SendMsg singleLineButton(String text, String point) {

        super.singleLineButton(text, point);

        return this;

    }

    @Override
    public SendMsg singleLineButton(String text, String point, TwiAccount acc) {

        super.singleLineButton(text, point, acc);

        return this;

    }

    @Override
    public void exec() {

        if (replyButtonGroups.size() != 0) {
            
            LinkedList<KeyboardButton[]> groups = new LinkedList<>();

            for (ReplyButtonGroup group : replyButtonGroups) {

                groups.add(group.getButtonArray());

            }

            send.replyMarkup(new ReplyKeyboardMarkup(groups.toArray(new KeyboardButton[groups.size()][])));
            
            
        } else if (inlineKeyBoardGroups.size() != 0) {

            LinkedList<InlineKeyboardButton[]> groups = new LinkedList<>();

            for (InlineButtonGroup group : inlineKeyBoardGroups) {

                groups.add(group.getButtonArray());

            }
            
            send.replyMarkup(new InlineKeyboardMarkup(groups.toArray(new InlineKeyboardButton[groups.size()][])));

        } else if (keyboaordType == 1) {
            
            send.replyMarkup(new ReplyKeyboardHide());
            
        } else if (keyboaordType == 2) {
            
            send.replyMarkup(new ReplyKeyboardRemove());
            
        }
        
        Constants.bot.execute(send, new Callback<SendMessage,SendResponse>() {

                @Override
                public void onResponse(SendMessage p1, SendResponse p2) {
                 
                    if (!p2.isOk()) {
                        
                        StaticLog.error(p2.errorCode() + ":" + p2.description());
                        
                    }
                    
                }

                @Override
                public void onFailure(SendMessage p1, IOException p2) {
                    
                    p2.printStackTrace();
                    
                }
            });

    }

}
