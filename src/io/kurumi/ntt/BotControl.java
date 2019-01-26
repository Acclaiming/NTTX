package io.kurumi.ntt;

import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.ui.request.*;
import java.util.*;
import com.pengrad.telegrambot.*;
import com.pengrad.telegrambot.request.*;

public class BotControl {
    
    public static HashMap<String,UserBot> bots = new HashMap<>();
    
    public static AbsResuest process(String bot,Update update) {
        
        if (!bots.containsKey(bot)) {
            
            return new Pack<DeleteWebhook> (new DeleteWebhook());
            
        }
        
        return bots.get(bot).processUpdate(update);
        
    }
    
    public static void stopAll() {
        
        for (Map.Entry<String,UserBot> bot : bots.entrySet()) {
            
            bot.getValue().deleteHook();
            
        }
        
        bots.clear();
        
    }
    
}
