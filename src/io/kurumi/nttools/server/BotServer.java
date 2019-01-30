package io.kurumi.nttools.server;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;
import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.DeleteWebhook;
import io.kurumi.nttools.fragments.Fragment;
import io.kurumi.nttools.fragments.MainFragment;
import java.io.IOException;
import java.util.HashMap;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.request.Method;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

public class BotServer extends NanoHTTPD {

    public BotServer(MainFragment bot) { super(bot.serverPort); }
    
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

        if (URLUtil.url(session.getUri()).getPath().equals("/callback")) {
            
            HashMap<String, String> params = HttpUtil.decodeParamMap(StrUtil.subAfter(session.getUri(), "?", true), "UTF-8");

            if (params == null) return null;

            String requestToken = params.get("oauth_token");
            String oauthVerifier = params.get("oauth_verifier");
            
            if (AuthCache.cache.containsKey(requestToken)) {
                
                AuthCache.cache.remove(requestToken).onAuth(oauthVerifier);
                
            }
           
        }
        
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
    
    public static BotServer INSTANCE;
    
}
