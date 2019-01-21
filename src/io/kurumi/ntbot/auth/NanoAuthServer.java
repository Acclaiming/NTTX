package io.kurumi.ntbot.auth;

import cn.hutool.core.util.*;
import io.kurumi.ntbot.twitter.*;
import java.net.*;
import org.nanohttpd.protocols.http.*;
import org.nanohttpd.protocols.http.response.*;
import io.kurumi.ntbot.md.*;

public class NanoAuthServer extends NanoHTTPD {

    private AuthManager manager;

    public NanoAuthServer(AuthManager manager, int port) {
        super(port);
        this.manager = manager;
    }

    @Override
    public Response handle(IHTTPSession session) {

        URL url = URLUtil.url(session.getUri());

        if (url.getPath().equals("/check")) return Response.newFixedLengthResponse("ok");
        if (url.getPath().startsWith("/callback")) return callback(session);



        return super.handle(session);
    }

    private Response callback(IHTTPSession session) {

        TwiAccount account = manager.authByUrl(session.getUri());

        String[] msg;

        if (account != null) {

            msg = new String[] {

                "# NTTBot 添加账号","",

                "失败了 T^T 乃可以返回Bot重试"
            };

        } else {

            msg = new String[] {

                "# NTTBot 添加账号","",

                "Twitter 账号 : " + account.getFormatedName() + " 添加成功！","",

                "请返回Bot (◦˙▽˙◦)"

            };

        }

        return Response.newFixedLengthResponse(Markdown.toHtml(ArrayUtil.join(msg, "\n")));
    }


}
