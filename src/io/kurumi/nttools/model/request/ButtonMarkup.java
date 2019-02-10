package io.kurumi.nttools.model.request;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import io.kurumi.nttools.utils.CData;
import java.util.LinkedList;
import io.kurumi.nttools.twitter.TwiAccount;
import io.kurumi.nttools.utils.UserData;

public class ButtonMarkup extends LinkedList<ButtonLine> {
    
    public ButtonLine newButtonLine() {

        ButtonLine ButtonLine = new ButtonLine();

        add(ButtonLine);

        return ButtonLine;

    }
    
    public void newButtonLine(String text,String point,String index) {

        CData data = new CData();

        data.setPoint(point);
        
        data.setindex(index);

        newButtonLine().newButton(text,data);

    }
    
    public void newButtonLine(String text,String point ,UserData user,TwiAccount account) {

        CData data = new CData();

        data.setPoint(point);
        
        data.setUser(user,account);

        newButtonLine().newButton(text,data);

    }
    
    public void newButtonLine(String text,String point,String index ,UserData user,TwiAccount account) {

        CData data = new CData();

        data.setPoint(point);
        
        data.setindex(index);

        data.setUser(user,account);

        newButtonLine().newButton(text,data);

    }

    public void newButtonLine(String text,String point) {

        CData data = new CData();
        
        data.setPoint(point);
        
        newButtonLine().newButton(text,data);

    }
    
    
    public void newButtonLine(String text,CData data) {

        newButtonLine().newButton(text,data);

    }

    public void newUrlButtonLine(String text,String url) {

        newButtonLine().newUrlButton(text,url);

    }

    public InlineKeyboardMarkup markup() {

        LinkedList<InlineKeyboardButton[]> buttons = new LinkedList<>();

        for(ButtonLine ButtonLine : this)  {

            buttons.add(ButtonLine.toArray());

        }

        return new InlineKeyboardMarkup(buttons.toArray(new InlineKeyboardButton[size()][]));

    }
    
}
