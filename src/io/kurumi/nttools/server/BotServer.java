package io.kurumi.nttools.server;

import cn.hutool.core.util.*;
import com.pengrad.telegrambot.*;
import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.ui.*;
import io.kurumi.ntt.ui.request.*;
import io.kurumi.nttools.bots.*;
import java.io.*;
import java.util.*;
import org.nanohttpd.protocols.http.*;
import org.nanohttpd.protocols.http.request.*;
import org.nanohttpd.protocols.http.response.*;
import com.pengrad.telegrambot.request.*;
import io.kurumi.nttools.*;

public class BotServer extends NanoHTTPD {

    public BotServer() { super(Configuration.localPort); }
    
    public static HashMap<String,Fragment> bots = new HashMap<>();
    
    public String readBodyString(IHTTPSession session) {

        int contentLength = Integer.parseInt(session.getHeaders().get("content-length"));
        byte[] buf = new byte[contentLength];
        try {
            session.getInputStream().read(buf, 0, contentLength);
            return StrUtil.str(buf,CharsetUtil.CHARSET_UTF_8);
        } catch (IOException ex) {
        }

        return null;

    }
    
    @Override
    public Response handle(IHTTPSession session) {
        
        Response C404 = Response.newFixedLengthResponse(Status.NOT_FOUND, MIME_PLAINTEXT, "");

        if (session.getMethod() != Method.POST) return C404;

        String path = URLUtil.url(session.getUri()).getPath();

        path = StrUtil.subAfter(path, "/", true);

        Update update = BotUtils.parseUpdate(readBodyString(session));

        Fragment fragment = bots.get(path);
        
        if (fragment == null) {
            
            new TelegramBot(path).execute(new DeleteWebhook());
            
            return C404;
            
        }
        
        try {
        
        fragment.processUpdate(update);

        } catch (Exception exc) {
            
           exc.printStackTrace();
            
        }
        
        return Response.newFixedLengthResponse("");
    }
    
    public static final BotServer INSTANCE = new BotServer();
    
}
