package io.kurumi.ntt.fragment.abs.request;

import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;

import java.util.LinkedList;

public class Keyboard extends LinkedList<KeyboradButtonLine> {

    public KeyboradButtonLine newButtonLine() {

        KeyboradButtonLine line = new KeyboradButtonLine();

        add(line);

        return line;

    }

    public void newButtonLine(String text) {

        newButtonLine().newButton(text);

    }

    public void newRequestLocationButtonLine(String text) {

        newButtonLine().newRequestLocationButton(text);

    }

    public void newRequestContactButtonLine(String text) {

        newButtonLine().newRequestContactButton(text);

    }


    public ReplyKeyboardMarkup markup() {

        LinkedList<KeyboardButton[]> buttons = new LinkedList<>();

        for (KeyboradButtonLine line : this) {

            buttons.add(line.toArray());

        }

        return new ReplyKeyboardMarkup(buttons.toArray(new KeyboardButton[size()][]));


    }

}
