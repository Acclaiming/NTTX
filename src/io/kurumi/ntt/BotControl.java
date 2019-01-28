package io.kurumi.ntt;

import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.request.*;
import io.kurumi.ntt.bots.*;
import io.kurumi.ntt.ui.confs.*;
import io.kurumi.ntt.ui.request.*;
import java.util.*;
import com.pengrad.telegrambot.*;

public class BotControl {
    
    public static HashMap<UserData,LinkedList<UserBot>> bots = new HashMap<>();
    public static HashMap<String,TelegramUserBot> telegramBots = new HashMap<>();
    
    public static HashMap<UserBot,ConfRoot> confs = new HashMap<>();

    
    public static AbsResuest process(String bot,Update update) {
        
        if (!telegramBots.containsKey(bot)) {
            
            return new Pack<DeleteWebhook> (new DeleteWebhook());
            
        }
        
        return telegramBots.get(bot).processUpdate(update);
        
    }
    
    public static void stopAll() {
        
        for (LinkedList<UserBot> botList : bots.values()) {
            
            for (UserBot bot : botList) {
                
                bot.interrupt();
                
            }
            
        }
        
        bots.clear();
        
    }
    
}
