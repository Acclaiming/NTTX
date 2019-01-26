package io.kurumi.ntt;

import com.pengrad.telegrambot.*;
import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.ui.request.*;
import com.pengrad.telegrambot.request.*;

public abstract class UserBot {
    
    public UserData owner;
    public String token;
    public TelegramBot bot;
    
    public static final String UPDATE_TYPE_MESSAGE = "message";
    public static final String UPDATE_TYPE_EDITED_MESSAGE = "edited_message";
    public static final String UPDATE_TYPE_CHANNEL_POST = "channel_post";
    public static final String UPDATE_TYPE_EDITED_CHANNEL_POST = "edited_channel_post";
    public static final String UPDATE_TYPE_INLINE_QUERY = "inline_query";
    public static final String UPDATE_TYPE_CHOSEN_INLINE_RESULT = "chosen_inline_result";
    public static final String UPDATE_TYPE_CALLBACK_QUERY = "callback_query";
    public static final String UPDATE_TYPE_SKIPPING_QUERY = "shipping_query";
    public static final String UPDATE_TYPE_PRE_CHECKOUT_QUERY = "pre_checkout_query";
    
    public UserBot(UserData owner,String token) {
        
        this.owner = owner;
        this.token = token;
        this.bot = new TelegramBot(token);
        
        deleteHook();
        
    }
    
    public void deleteHook() {
        
        bot.execute(new DeleteWebhook());
        
    }
    
    public void setHook(String domain) {
        
        bot.execute(new SetWebhook().allowedUpdates(allowUpdates()).url("https://" + domain + "/" + token));
        
    }
    
    public abstract String[] allowUpdates();
    public abstract AbsResuest processUpdate(Update update);
    
}
