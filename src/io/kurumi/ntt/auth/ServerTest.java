package io.kurumi.ntt.auth;

import cn.hutool.core.lang.caller.*;
import cn.hutool.core.util.*;
import cn.hutool.http.*;
import io.kurumi.ntt.md.*;
import java.io.*;
import java.lang.reflect.*;
import org.nanohttpd.protocols.http.*;
import org.nanohttpd.protocols.http.response.*;

public class ServerTest extends NanoHTTPD {
    
    public ServerTest() {
        
        super("127.0.0.1",3399);
        
    }
    
    public static void main(String[] args) {
        
        try {

            Field caller = CallerUtil.class.getDeclaredField("INSTANCE");

            caller.setAccessible(true);

            caller.set(null, new StackTraceCaller());

        } catch (Exception e) {

            e.printStackTrace();

        }
        
        try {
            new ServerTest().start();
            
           HttpUtil.get("http://127.0.0.1:3399");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Response handle(IHTTPSession session) {
        // TODO: Implement this method
        
        String[] args = new String[] {
            
            "## 成功...？","",
            "大概....？ [N](https://kurumi.io)  ","",
            "*emmmmm*",
            
        };
        
        String str = Markdown.parsePage("test",ArrayUtil.join(args,"\n"));
        
        return Response.newFixedLengthResponse(str);
        
        
      //  return super.handle(session);
    }
    
    
    
}
