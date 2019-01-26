package io.kurumi.ntt.ui.model;

import com.pengrad.telegrambot.model.request.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.ui.*;
import java.util.*;

public class InlineButtonGroup extends LinkedList<InlineKeyboardButton> {

    public InlineButtonGroup newOpenUrlButton(String text,String url) {
        
        add(new InlineKeyboardButton(text).url(url));

        return this;
        
    }
    
    public InlineButtonGroup newButton(String text, String point,TwiAccount acc) {

        DataObject obj = new DataObject();

        obj.setPoint(point);

        obj.setUser(acc);
        
        newButton(text, obj);


        return this;

    }
    
    public InlineButtonGroup newButton(String text, String point) {

        DataObject obj = new DataObject();

        obj.setPoint(point);

        newButton(text, obj);


        return this;

    }

    public InlineButtonGroup newButton(String text, DataObject obj) {

        add(new InlineKeyboardButton(text).callbackData(obj.toString()));

        return this;

    }
    
    public InlineKeyboardButton[] getButtonArray() {
        
        return toArray(new InlineKeyboardButton[size()]);
        
    }

}
