package io.kurumi.nt.cmd.tg;

import io.kurumi.nt.*;
import com.pengrad.telegrambot.*;
import java.util.*;
import com.pengrad.telegrambot.model.*;

public class TgMain implements UpdatesListener {
    
    private NTUser user;

    public TgMain(NTUser user) {
        
        this.user = user;
        
    }
    
    public String getToken() {
        
        return user.userData.getStr("tg_bot_token");
        
    }
    
    public void setToken(String token) {
        
        user.userData.put("tg_bot_token",token);
        user.save();
        
    }
    
    public TelegramBot bot;
    
    public void init() {
        
        bot = new TelegramBot(getToken());
        
    }

    @Override
    public int process(List<Update> updates) {
        
        return CONFIRMED_UPDATES_ALL;
        
    }
    
}
