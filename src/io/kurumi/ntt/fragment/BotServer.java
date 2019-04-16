package io.kurumi.ntt.fragment;

import fi.iki.elonen.*;
import java.util.*;
import fi.iki.elonen.NanoHTTPD.*;
import org.apache.http.client.utils.*;
import cn.hutool.core.util.*;
import java.net.*;
import com.pengrad.telegrambot.*;
import java.io.*;
import com.pengrad.telegrambot.request.*;

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

        String botToken = url.getPath().substring(1);

        if (fragments.containsKey(botToken)) {
            
            fragments.get(botToken).processAsync(BotUtils.parseUpdate(new InputStreamReader(session.getInputStream())));
            
        } else {
            
            new TelegramBot(botToken).execute(new DeleteWebhook());
            
        }
        
        return newFixedLengthResponse("");
    }

}
