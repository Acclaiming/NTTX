package io.kurumi.ntt.ui.model;

import com.pengrad.telegrambot.model.request.*;
import io.kurumi.ntt.bots.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.ui.*;
import io.kurumi.ntt.ui.request.*;
import java.util.*;
import cn.hutool.core.util.*;

public class InlineButtonGroup extends LinkedList<InlineKeyboardButton> {   

    private AbsSendMsg msg;

    public InlineButtonGroup(AbsSendMsg msg) {

        this.msg = msg;

    }

    public InlineButtonGroup newOpenUrlButton(String text, String url) {

        add(new InlineKeyboardButton(text).url(url));

        return this;

    }
    
    public InlineButtonGroup newButton(String text, String point,String index) {

        DataObject obj = new DataObject();

        obj.setPoint(point);
        
        obj.setindex(index);

        newButton(text, obj);


        return this;

    }
    
    public InlineButtonGroup newButton(String text,String point,UserBot bot) {
    
        DataObject obj = new DataObject();

        obj.setPoint(point);

        obj.setBot(bot);

        newButton(text, obj);


        return this;

    }  
        
    public InlineButtonGroup newButton(String text, String point, TwiAccount acc) {

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

        msg.processObject(obj);
        
        add(new InlineKeyboardButton(text).callbackData(obj.toString()));

        return this;

    }

    public InlineKeyboardButton[] getButtonArray() {

        return toArray(new InlineKeyboardButton[size()]);

    }

}
