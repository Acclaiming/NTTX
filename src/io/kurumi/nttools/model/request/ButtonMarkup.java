package io.kurumi.nttools.model.request;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import io.kurumi.nttools.utils.CData;
import java.util.LinkedList;

public class ButtonMarkup {

    public LinkedList<ButtonLine> lines = new LinkedList<>();

    public ButtonLine newButtonLine() {

        ButtonLine ButtonLine = new ButtonLine();

        lines.add(ButtonLine);

        return ButtonLine;

    }

    public void ButtonLine(String text,CData data) {

        newButtonLine().newButton(text,data);

    }

    public void newUrlButtonLine(String text,String url) {

        newButtonLine().newUrlButton(text,url);

    }

    public InlineKeyboardMarkup markup() {

        LinkedList<InlineKeyboardButton[]> buttons = new LinkedList<>();

        for(ButtonLine ButtonLine : lines)  {

            buttons.add(ButtonLine.toArray());

        }

        return new InlineKeyboardMarkup(buttons.toArray(new InlineKeyboardButton[lines.size()][]));

    }
    
}
