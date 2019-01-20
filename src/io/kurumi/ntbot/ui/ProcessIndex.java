package io.kurumi.ntbot.ui;

import com.pengrad.telegrambot.model.*;
import io.kurumi.ntbot.*;
import io.kurumi.ntbot.ui.ext.*;

public class ProcessIndex {
    
    public static void processUpdate(Update update) {
        
        processProvateMessage(update.message());
        processCallbackQuery(update.callbackQuery());
        
    }

    public static void processProvateMessage(Message message) {

        if (message == null) return;
        
        UserData userData = Constants.data.getUser(message.from());
        
        if ("".equals(userData.point)) {}

    }
    
    private static void processCallbackQuery(CallbackQuery callbackQuery) {
       
        if (callbackQuery == null) return;
        
    }

    
    
}
