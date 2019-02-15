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
    
    public LinkedList<ServerFragment> fragments = new LinkedList<>();
    
    @Override
    public Response handle(IHTTPSession session) {
        
        Response C404 = Response.newFixedLengthResponse(Status.NOT_FOUND, MIME_PLAINTEXT, "");
        
        for(ServerFragment fragment : fragments) {
            
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
        
                return Response.newFixedLengthResponse("");
        
        */
        
    }
    
}
