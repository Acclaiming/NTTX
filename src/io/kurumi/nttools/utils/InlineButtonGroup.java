package io.kurumi.nttools.utils;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import io.kurumi.nttools.twitter.TwiAccount;
import java.util.LinkedList;

public class InlineButtonGroup extends LinkedList<InlineKeyboardButton> {   

    public InlineButtonGroup newOpenUrlButton(String text, String url) {

        add(new InlineKeyboardButton(text).url(url));

        return this;

    }
    
    

    public InlineButtonGroup newButton(String text, String data) {

        add(new InlineKeyboardButton(text).callbackData(data));

        return this;

    }

    public InlineKeyboardButton[] getButtonArray() {

        return toArray(new InlineKeyboardButton[size()]);

    }

}
