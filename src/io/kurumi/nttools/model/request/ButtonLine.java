package io.kurumi.nttools.model.request;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import io.kurumi.nttools.utils.CData;
import java.util.LinkedList;

public class ButtonLine extends LinkedList<InlineKeyboardButton> {

    public ButtonLine newButton(String text,CData data) {

        add(new InlineKeyboardButton(text).callbackData(data.toString()));

        return this;

    }

    public ButtonLine newUrlButton(String text,String url) {

        add(new InlineKeyboardButton(text).url(url));

        return this;

    }

    public InlineKeyboardButton[] toArray() {

        return toArray(new InlineKeyboardButton[size()]);

    }

}
        
