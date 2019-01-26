package io.kurumi.ntt.webhook;

import org.nanohttpd.protocols.http.*;

public class WebHookServer extends NanoHTTPD {
    
    public WebHookServer(String domain,int port) {
        super(domain,port);
    }
    
}
