package io.kurumi.ntbot.ui.callback;

import com.pengrad.telegrambot.model.*;
import java.util.*;
import io.kurumi.ntbot.ui.ext.*;
import io.kurumi.ntbot.ui.entries.*;

public class InlineCallback {

    
    public static void process(CallbackQuery callbackQuery) {
       
        if (callbackQuery == null) return;
        
        String data = callbackQuery.data();
        
        UserInterface ui =  Constants.data.getUser(callbackQuery.from()).getInterface();

        if (data.startsWith("0|")) {
        
        ui.processCallback(data,callbackQuery);
        
        }
        
    }
    
}
