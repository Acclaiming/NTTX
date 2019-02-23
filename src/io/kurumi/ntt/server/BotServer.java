package io.kurumi.ntt.server;

import io.kurumi.ntt.BotConf;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.util.LinkedList;

/*

public class BotServer extends NanoHTTPD {

    public static final BotServer INSTACNCE = new BotServer();
    public LinkedList<ServerFragment> fragments = new LinkedList<>();
    
    public BotServer() {
        super(BotConf.LOCAL_PORT);
    }

    @Override
    public Response handle(IHTTPSession session) {

        Response C404 = Response.newFixedLengthResponse(Status.NOT_FOUND, MIME_PLAINTEXT, "");

        for (ServerFragment fragment : fragments) {

            Response resp = fragment.handle(session);

            if (resp != null) return resp;

        }

        return C404;

    }

}

*/
