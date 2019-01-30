package io.kurumi.nttools.bots;

import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.*;
import java.util.*;
import com.pengrad.telegrambot.request.*;
import io.kurumi.nttools.*;

public abstract class Fragment {
    
    public final String token;
    public final TelegramBot bot;

    public Fragment(String token) {
        this.token = token;
        bot = new TelegramBot(token);
    }
    
    public void startGetUpdates() {
        
        deleteWebHook();
        
        bot.setUpdatesListener(new UpdatesListener() {
                @Override
                public int process(List<Update> updates) {
                    
                    for (Update update : updates) {
                        
                       processUpdate(update);
                       
                    }
                    
                    return CONFIRMED_UPDATES_ALL;
                }
            });
        
    }
    
    public void setWebHook() {
        
        bot.execute(new SetWebhook().url("https://" + Configuration.serverDomain + "/" + token));
        
    }
    
    public void deleteWebHook() {
        
        bot.execute(new DeleteWebhook());
        
    }

    public abstract void processUpdate(Update update);
    
}
