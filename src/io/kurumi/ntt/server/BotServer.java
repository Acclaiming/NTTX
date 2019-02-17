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
        
    }
    
}
