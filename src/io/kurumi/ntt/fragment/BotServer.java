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

    public String readBody(IHTTPSession session)  {

        int contentLength = Integer.parseInt(session.getHeaders().get("content-length"));
        byte[] buf = new byte[contentLength];

        try {
            session.getInputStream().read(buf,0,contentLength);

            return StrUtil.utf8Str(buf);

        } catch ( IOException e2 ) { }

        return null;

    }



    @Override
    public Response serve(IHTTPSession session) {

        URL url = URLUtil.url(session.getUri());

        String botToken = url.getPath().substring(1);

        if (fragments.containsKey(botToken)) {

            fragments.get(botToken).processAsync(BotUtils.parseUpdate(readBody(session)));

            return newFixedLengthResponse("");

        } else {

            try {

                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR,MIME_PLAINTEXT,"ERROR");

            } finally {

                new TelegramBot(botToken).execute(new DeleteWebhook());
                
            }

        }


    }

}
