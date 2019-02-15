package io.kurumi.ntt.server;

import java.util.LinkedList;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;
import io.kurumi.ntt.BotConf;

public class BotServer extends NanoHTTPD {

    public BotServer() { super(BotConf.LOCAL_PORT); }
    
    public static final BotServer INSTACNCE = new BotServer();
    
    public LinkedList<BotFragment> fragments = new LinkedList<>();
    
    @Override
    public Response handle(IHTTPSession session) {
        
        Response C404 = Response.newFixedLengthResponse(Status.NOT_FOUND, MIME_PLAINTEXT, "");
        
        for(BotFragment fragment : fragments) {
            
            Response resp = fragment.handle(session);
            
            if (resp != null) return resp;

        }
        
        return C404;
        
        /*
        
        
        if (URLUtil.url(session.getUri()).getPath().equals("/callback")) {
            
            
            String requestToken = session.getParms().get("oauth_token");
            String oauthVerifier = session.getParms().get("oauth_verifier");
            
            if (AuthCache.cache.containsKey(requestToken)) {
                
                AuthCache.cache.remove(requestToken).onAuth(oauthVerifier);
                
                return Response.newFixedLengthResponse(Markdown.parsePage("请返回Bot","#NTTBot 账号认证 ~\n到底成功了没 ？ (´▽`ʃƪ)"));
                
                
            } else {
                
                return Response.newFixedLengthResponse(Markdown.parsePage("请返回Bot","#NTTBot 账号认证 ~\n这个认证链接过期了啦 ~\n是不是刷新了界面/登录了两次 ？ (´▽`ʃƪ)\n\n\nrequestToken : " + requestToken + "\ncaches : \n" + ArrayUtil.join(AuthCache.cache.keySet().toArray(),"\n")));
                
                
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
        
        */
        
    }
    
}
