package io.kurumi.ntt.fragment;

import cn.hutool.core.util.*;
import com.pengrad.telegrambot.*;
import com.pengrad.telegrambot.request.*;
import fi.iki.elonen.*;
import java.io.*;
import java.net.*;
import java.util.*;
import io.kurumi.ntt.utils.*;
import fi.iki.elonen.NanoHTTPD.*;
import io.kurumi.ntt.*;

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

	String getDonateUrl(int amount) {
		
		if (amount < 5) amount = 5;
		
		String donateUrl = DonateUtil.ccAlipay(amount);
		
		if (donateUrl == null) {
			
			if (!DonateUtil.ccLogin(Env.get("donate.cc.email"),Env.get("donate.cc.password"))) {
				
				BotLog.debug("login failed");
				
				return "about:blank";
				
			}
			
			donateUrl = DonateUtil.ccAlipay(amount);
			
			if (donateUrl == null) return "about:blank";
			
		}
		
		return donateUrl;
		
	}
	

    @Override
    public Response serve(IHTTPSession session) {

        URL url = URLUtil.url(session.getUri());
		
		if (url.getPath().startsWith("/donate")) {
			
			int amont = session.getParms().containsKey("amount") ? Integer.parseInt(session.getParms().get("amount")) :5;
			
			return redirectTo(getDonateUrl(amont));
			
		}
		
		System.out.println(url.getPath());
		
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
	
	public Response redirectTo(String url) {
		
		Response resp = newFixedLengthResponse(Response.Status.REDIRECT_SEE_OTHER,MIME_HTML,"<html><head><titile>Redirecting...</title></head><body></body></html>");

		resp.addHeader("Location",url);
		
		return resp;
		
	}

}
