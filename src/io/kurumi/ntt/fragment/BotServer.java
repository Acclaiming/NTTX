package io.kurumi.ntt.fragment;

import cn.hutool.core.util.*;
import com.pengrad.telegrambot.*;
import com.pengrad.telegrambot.request.*;
import fi.iki.elonen.*;
import java.io.*;
import java.net.*;
import java.util.*;
import io.kurumi.ntt.utils.*;

public class BotServer extends NanoHTTPD {
    
    public static HashMap<String,BotFragment> fragments = new HashMap<>();
    public static BotServer INSTANCE;
    public String domain;
    
    public BotServer(int port,String domain) {
        
        super(port);
        
        this.domain = domain;
        
    }

    @Override
    public Response serve(IHTTPSession session) {
        
        URL url = URLUtil.url(session.getUri());

        BotLog.debug(url.getPath());
        
        String botToken = url.getPath().substring(1);

        if (fragments.containsKey(botToken)) {
            
            fragments.get(botToken).processAsync(BotUtils.parseUpdate(new InputStreamReader(session.getInputStream())));
            
        } else {
            
            new TelegramBot(botToken).execute(new DeleteWebhook());
            
        }
        
        return newFixedLengthResponse("");
    }

}
