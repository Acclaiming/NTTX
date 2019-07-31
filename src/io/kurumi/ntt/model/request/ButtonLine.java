package io.kurumi.ntt.model.request;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;

import java.util.LinkedList;

import cn.hutool.core.util.*;

public class ButtonLine extends LinkedList<InlineKeyboardButton> {

    public ButtonLine newButton(String text, String point, Object... data) {

        add(new InlineKeyboardButton(text).callbackData(ArrayUtil.join(ArrayUtil.insert(data, 0, point), ",")));

        return this;

    }

    public ButtonLine newUrlButton(String text, String url) {

        add(new InlineKeyboardButton(text).url(url));

        return this;

    }

    public ButtonLine newInlineButton(String text, String query) {

        add(new InlineKeyboardButton(text).switchInlineQuery(query));

        return this;

    }

    public ButtonLine newCurrentInlineButton(String text, String query) {

        add(new InlineKeyboardButton(text).switchInlineQueryCurrentChat(query));

        return this;

    }


    @Override
    public InlineKeyboardButton[] toArray() {

        return toArray(new InlineKeyboardButton[size()]);

    }

}
        
