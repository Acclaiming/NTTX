package io.kurumi.nttools.model.request;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import io.kurumi.nttools.twitter.TwiAccount;
import io.kurumi.nttools.utils.CData;
import io.kurumi.nttools.utils.UserData;
import java.util.LinkedList;

public class ButtonLine extends LinkedList<InlineKeyboardButton> {

    public ButtonLine newButton(String text,CData data) {

        add(new InlineKeyboardButton(text).callbackData(data.toString()));

        return this;

    }
    
    public ButtonLine newButton(String text,String point,String index) {

        CData data = new CData();

        data.setPoint(point);

        data.setindex(index);

        newButton(text,data);
        
        return this;

    }

    public ButtonLine newButton(String text,String point ,UserData user,TwiAccount account) {

        CData data = new CData();

        data.setPoint(point);

        data.setUser(user,account);

        newButton(text,data);
        
        return this;

    }

    public ButtonLine newButton(String text,String point,String index ,UserData user,TwiAccount account) {

        CData data = new CData();

        data.setPoint(point);

        data.setindex(index);

        data.setUser(user,account);

        newButton(text,data);
        
        return this;

    }

    public ButtonLine newButton(String text,String point) {

        CData data = new CData();

        data.setPoint(point);

        newButton(text,data);
        
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
        
