package io.kurumi.nttools.utils;

import java.util.*;
import com.pengrad.telegrambot.model.request.*;

public class ReplyButtonGroup extends LinkedList<KeyboardButton> {
    
    public ReplyButtonGroup newButton(String text) {
        
        add(new KeyboardButton(text));
        
        return this;
        
    }
    
    public ReplyButtonGroup newRequestContactButton(String text) {
        
        add(new KeyboardButton(text).requestContact(true));
        
        return this;
        
    }
    
    public ReplyButtonGroup newRequestLocationButton(String text) {

        add(new KeyboardButton(text).requestLocation(true));

        return this;

    }
    
    public KeyboardButton[] getButtonArray() {
        
        return toArray(new KeyboardButton[size()]);
        
    }
    
}
