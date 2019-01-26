package io.kurumi.ntt.ui.request;

import cn.hutool.core.util.*;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.ui.*;
import io.kurumi.ntt.ui.model.*;
import java.util.*;
import io.kurumi.ntt.ui.ext.*;

public class EditMsg extends AbsSendMsg {

    private EditMessageText edit;
    
    public EditMsg(Message msg, String... editMsg) {

        String contnet = msg.text();

        if (editMsg.length != 0) {

            contnet = ArrayUtil.join(editMsg, "\n");

        }
        
        edit = new EditMessageText(msg.chat(),msg.messageId(),contnet);

    }

    @Override
    public EditMsg html() {
        
        edit.parseMode(ParseMode.HTML);
        
        return this;
        
    }

    @Override
    public EditMsg markdown() {
        
        edit.parseMode(ParseMode.Markdown);
        
        return this;
        
    }

    @Override
    public EditMsg disableWebPagePreview() {
        
        edit.disableWebPagePreview(true);
        
        return this;
        
    }

    @Override
    public EditMsg disableNotification() {
       
        edit.disableWebPagePreview(true);
        
        return this;
        
    }

    @Override
    public EditMsg singleLineButton(String text, DataObject obj) {
        
        super.singleLineButton(text, obj);
        
        return this;
        
    }

    @Override
    public EditMsg singleLineButton(String text, String point) {
      
        super.singleLineButton(text, point);
        
        return this;
        
    }


    @Override
    public EditMsg singleLineButton(String text, String point, TwiAccount acc) {
        
        super.singleLineButton(text, point, acc);
        
        return this;
        
    }

    @Override
    public void exec() {
       
        if (inlineKeyBoardGroups.size() != 0) {

            LinkedList<InlineKeyboardButton[]> markups = new LinkedList<>();

            for (InlineButtonGroup group : inlineKeyBoardGroups) {

                markups.add(group.getButtonArray());

            }

            edit.replyMarkup(new InlineKeyboardMarkup(markups.toArray(new InlineKeyboardButton[markups.size()][])));
            
            }
            
            Constants.bot.execute(edit);
        
   }

}
