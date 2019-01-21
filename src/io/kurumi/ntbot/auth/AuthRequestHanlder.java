package io.kurumi.ntbot.auth;

import io.kurumi.nt.twitter.*;
import org.tio.http.common.*;
import org.tio.http.common.handler.*;
import cn.hutool.core.util.*;
import io.kurumi.ntbot.twitter.*;

public class AuthRequestHanlder implements HttpRequestHandler {

    private AuthManager manager;

    public AuthRequestHanlder(AuthManager manager) {
        this.manager = manager;
    }
    
	@Override
	public HttpResponse handler(HttpRequest request) throws Exception {
		
        switch (request.getRequestLine().path) {
            
            case "/check" : {
                
                    HttpResponse resp =  new HttpResponse(request);
                    resp.setStatus(HttpResponseStatus.C200);
                    resp.setCharset(CharsetUtil.UTF_8);
                    resp.setBody("ok".getBytes(CharsetUtil.CHARSET_UTF_8));
                    return resp;

            }
            
			case "/callback" : {

					String requestToken = request.getParam("oauth_token");
                    String oauthVerifier = request.getParam("oauth_verifier");
                    
                    if (requestToken == null || oauthVerifier == null) {
                        
                        HttpResponse resp =  new HttpResponse(request);
                        resp.setCharset(CharsetUtil.UTF_8);
                        resp.setStatus(HttpResponseStatus.C302);
                        resp.addHeader(HeaderName.Location,HeaderValue.from(manager.newRequest()));
                        
                        return resp;

                    }
                   
                    TwiAccount acc = manager.auth(requestToken,oauthVerifier);
                    
                    
                    if (acc == null) {
                        
                       
                        
                        HttpResponse resp =  new HttpResponse(request);
                        resp.setCharset(CharsetUtil.UTF_8);
                        resp.setStatus(HttpResponseStatus.C302);
                        resp.addHeader(HeaderName.Location,HeaderValue.from(manager.newRequest()));

                        return resp;
                        
                    }
					
				}
                
                }
                

        return resp404(request,request.getRequestLine());
        
        
        

	}

	@Override
	public HttpResponse resp404(HttpRequest request, RequestLine p2) throws Exception {
	 
        HttpResponse resp =  new HttpResponse(request);
        resp.setStatus(HttpResponseStatus.C404);
        return resp;
        
	}

	@Override
	public HttpResponse resp500(HttpRequest request, RequestLine p2, Throwable p3) throws Exception {
		HttpResponse resp =  new HttpResponse(request);
        resp.setStatus(HttpResponseStatus.C500);
        return resp;
        
	}

	@Override
	public HttpConfig getHttpConfig(HttpRequest request) {
		return request.getHttpConfig();
	}

	@Override
	public void clearStaticResCache() {
	}

}
