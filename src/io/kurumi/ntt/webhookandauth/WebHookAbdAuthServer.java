package io.kurumi.ntt.webhookandauth;

import cn.hutool.core.util.*;
import com.pengrad.telegrambot.*;
import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.md.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.ui.*;
import io.kurumi.ntt.ui.request.*;
import java.io.*;
import java.net.*;
import org.nanohttpd.protocols.http.*;
import org.nanohttpd.protocols.http.response.*;

public class WebHookAbdAuthServer extends NanoHTTPD {
    
    private String domain;
    
    public WebHookAbdAuthServer(String domain,int port) {
        super(domain,port);
        this.domain = domain;
    }

    @Override
    public Response handle(IHTTPSession session) {
        
        System.out.println(session.getUri());
       // System.out.println(session.getInputStream());
        
        URL url = URLUtil.url(session.getUri());

        switch (url.getPath()) {

                case "/": return main(session);

                case "/check": return Response.newFixedLengthResponse("ok");

                case "/callback" : return callback(session);

                case "/success" : return success(session);

                case "/failed" : return failed(session);

        }
        
     //   if (!"application/json".equals(session.getHeaders().get("Content-Type"))) return super.handle(session);

        String path = URLUtil.url(session.getUri()).getPath();

        path = StrUtil.subAfter(path,"/",true);
        
        Update update = BotUtils.parseUpdate(new InputStreamReader(session.getInputStream()));

        AbsResuest req;
        
        if (Constants.data.botToken.equals(path)) {

            req = ProcessIndex.processUpdate(update);

        } else {

            req = BotControl.process(path,update);

        }
        
        if (req != null) {

            Response resp = Response.newFixedLengthResponse(req.toWebHookResp());

            resp.setMimeType("application/json");
            
            return resp;

        }

        return Response.newFixedLengthResponse("");
        
    }

    private Response main(IHTTPSession session) {

        Response resp = Response.newFixedLengthResponse(Status.REDIRECT_SEE_OTHER, MIME_PLAINTEXT, "");

        resp.addHeader("Location", "https://github.com/HiedaNaKan/NTTools");

        return resp;

    }

    private Response success(IHTTPSession session) {

        String[] msg = new String[] {

            "# NTTBot 添加账号","",

            "Twitter 账号 : " + URLUtil.decode(session.getParms().get("user")) + " 添加成功！","",

            "请返回Bot( (◦˙▽˙◦)"

        };

        String page = Markdown.parsePage("成功 \\(≧▽≦)/", ArrayUtil.join(msg, "\n"));

        return Response.newFixedLengthResponse(page);


    }

    private Response failed(IHTTPSession session) {

        String[] msg = new String[] {

            "# NTTBot 添加账号","",

            "失败了 T^T 乃可以返回Bot重试",

        };

        String page = Markdown.parsePage("失败了呢.. T^T ", ArrayUtil.join(msg, "\n"));

        return Response.newFixedLengthResponse(page);

    }



    private Response callback(IHTTPSession session) {

        String oauth_token = session.getParms().get("oauth_token");
        String oauth_verifier = session.getParms().get("oauth_verifier");

        TwiAccount account = Constants.authandwebhook.auth(oauth_token, oauth_verifier);

        Response resp = Response.newFixedLengthResponse(Status.REDIRECT_SEE_OTHER, MIME_PLAINTEXT, "");

        if (account == null) {

            resp.addHeader("Location", domain + "/failed");

            return resp;

        } else {

            resp.addHeader("Location", domain + "/success?user=" + URLUtil.encode(account.getFormatedName()));

            return resp;

        }

    }
    
}
