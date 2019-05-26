package io.kurumi.ntt.model.request;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import io.kurumi.ntt.utils.CData;

import java.util.LinkedList;
import cn.hutool.core.util.*;

public class ButtonMarkup extends LinkedList<ButtonLine> {

    public ButtonLine newButtonLine() {

        ButtonLine ButtonLine = new ButtonLine();

        add(ButtonLine);

        return ButtonLine;

    }

    public void newButtonLine(String text, String point, Object... data) {

        newButtonLine().newButton(text, ArrayUtil.join(ArrayUtil.insert(data,0,point),","));

    }
    
    public void newButtonLine(String text, String point) {

        CData data = new CData();

        data.setPoint(point);

        newButtonLine().newButton(text, data);

    }

    public void newUrlButtonLine(String text, String url) {

        newButtonLine().newUrlButton(text, url);

    }

    public InlineKeyboardMarkup markup() {

        LinkedList<InlineKeyboardButton[]> buttons = new LinkedList<>();

        for (ButtonLine ButtonLine : this) {

            buttons.add(ButtonLine.toArray());

        }

        return new InlineKeyboardMarkup(buttons.toArray(new InlineKeyboardButton[size()][]));

    }

}
