package io.kurumi.ntt.fragment.abs.request;

import com.pengrad.telegrambot.model.request.KeyboardButton;

import java.util.LinkedList;

public class KeyboradButtonLine extends LinkedList<KeyboardButton> {

    public KeyboradButtonLine newButton(String text) {

        add(new KeyboardButton(text));

        return this;

    }

    public KeyboradButtonLine newRequestLocationButton(String text) {

        add(new KeyboardButton(text).requestLocation(true));

        return this;

    }

    public KeyboradButtonLine newRequestContactButton(String text) {

        add(new KeyboardButton(text).requestContact(true));

        return this;

    }

    @Override
    public KeyboardButton[] toArray() {

        return toArray(new KeyboardButton[size()]);

    }

}
