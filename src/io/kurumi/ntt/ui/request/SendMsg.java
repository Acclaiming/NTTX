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
    public TelegramBot bot = Constants.bot;

    public SendMsg(Chat chat, String... sendMsg) {

        this(chat.id(),sendMsg);
    }
    
    public SendMsg(long chatId, String... sendMsg) {

        send = new SendMessage(chatId, ArrayUtil.join(sendMsg, "\n"));

    }

    public SendMsg(Message msg, String... sendMsg) {

        this(msg.chat(), sendMsg);
        send.replyToMessageId(msg.messageId());

    }
    
    public SendMsg(TelegramBot bot,Chat chat, String... sendMsg) {

        this(chat,sendMsg);
        this.bot = bot;
        
    }

    public SendMsg(TelegramBot bot,Message msg, String... sendMsg) {

        this(msg, sendMsg);
        this.bot = bot;

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
    
    private void init() {
        
        if (replyButtonGroups.size() != 0) {

            KeyboardButton[][]  markup = new KeyboardButton[replyButtonGroups.size()][];

            for(int index = 0;index < replyButtonGroups.size();index ++) {

                markup[index] = replyButtonGroups.get(index).getButtonArray();

            }

            send.replyMarkup(new ReplyKeyboardMarkup(markup));


        } else if (inlineKeyBoardGroups.size() != 0) {

            InlineKeyboardButton[][]  markup = new InlineKeyboardButton[inlineKeyBoardGroups.size()][];

            for(int index = 0;index < inlineKeyBoardGroups.size();index ++) {

                markup[index] = inlineKeyBoardGroups.get(index).getButtonArray();

            }

            send.replyMarkup(new InlineKeyboardMarkup(markup));

        } else if (keyboaordType == 1) {

            send.replyMarkup(new ReplyKeyboardHide());

        } else if (keyboaordType == 2) {

            send.replyMarkup(new ReplyKeyboardRemove());

        }
        
    }

    @Override
    public SendResponse exec() {
        
        init();
        
        return bot.execute(send);

    }
    
    
    @Override
    public String toWebHookResp() {
        
        init();
        
        return send.toWebhookResponse();
        
    }

}

