package io.kurumi.ntt.server;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.response.Response;

public interface ServerFragment {

    public Response handle(IHTTPSession session);

}
