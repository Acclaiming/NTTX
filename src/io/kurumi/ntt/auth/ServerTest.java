package io.kurumi.ntt.auth;

import java.io.*;
import org.nanohttpd.protocols.http.*;
import org.nanohttpd.protocols.http.response.*;
import io.kurumi.ntt.md.*;
import cn.hutool.core.util.*;

public class ServerTest extends NanoHTTPD {
    
    public ServerTest() {
        
        super("127.0.0.1",3399);
        
    }
    
    public static void main(String[] args) {
        
        try {
            new ServerTest().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Response handle(IHTTPSession session) {
        // TODO: Implement this method
        
        String[] args = new String[] {
            
            "#成功...？","",
            "大概....？ [N](https://kurumi.io)",
            "*emmmmm*",
            
        };
        
        String str = Markdown.parsePage("test",ArrayUtil.join(args,"\n"));
        
        return Response.newFixedLengthResponse(str);
        
        
      //  return super.handle(session);
    }
    
    
    
}
