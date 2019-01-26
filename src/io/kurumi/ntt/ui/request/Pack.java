package io.kurumi.ntt.ui.request;

import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.*;
import io.kurumi.ntt.*;

public class Pack<T extends BaseRequest> implements AbsResuest {

    private T request;
    private TelegramBot bot = Constants.bot;

    public Pack(T requqst) {

        this.request = requqst;

    }
    
    public Pack(TelegramBot bot,T request) {
        
        this(request);
        this.bot = bot;
        
    }

    @Override
    public void exec() {
        
        bot.execute(request);
        
    }

    @Override
    public String toWebHookResp() {

        return request.toWebhookResponse();

    }
    
}
