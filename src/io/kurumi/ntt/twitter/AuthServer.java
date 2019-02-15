package io.kurumi.ntt.twitter;

import io.kurumi.ntt.server.ServerFragment;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.IHTTPSession;
import io.kurumi.ntt.server.BotServer;

public class AuthServer implements ServerFragment {
    
    public static final AuthServer INSTANCE = new AuthServer();
    
    @Override
    public Response handle(IHTTPSession session) {
       
       
        
        return null;
    }

}
