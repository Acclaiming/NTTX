package io.kurumi.ntt.model.request;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import io.kurumi.ntt.utils.CData;
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

    @Override
    public InlineKeyboardButton[] toArray() {

        return toArray(new InlineKeyboardButton[size()]);

    }

}
        
