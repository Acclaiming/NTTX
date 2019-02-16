package io.kurumi.ntt.model.request;

import java.util.LinkedList;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import io.kurumi.ntt.utils.CData;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;

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

        newButtonLine().newButton(text);

    }
    
    
    public ReplyKeyboardMarkup markup() {
        
        LinkedList<KeyboardButton[]> buttons = new LinkedList<>();

        for(KeyboradButtonLine line : this)  {

            buttons.add(line.toArray());

        }

        return new ReplyKeyboardMarkup(buttons.toArray(new KeyboardButton[size()][]));
        
        
    }
    
}
