package io.kurumi.ntt.fragment;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.DeleteWebhook;
import io.kurumi.ntt.server.ServerFragment;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.request.Method;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.NanoHTTPD;
import java.util.HashMap;

public class TGWebHookF implements ServerFragment {

    public static TGWebHookF INSTANCE = new TGWebHookF();

    public static HashMap<String,BotFragment> bots = new HashMap<>();

    @Override
    public Response handle(IHTTPSession session) {

        if (session.getMethod() != Method.POST) return null;

        String path = URLUtil.url(session.getUri()).getPath();

        path = StrUtil.subAfter(path, "/", true);

        Update update = BotUtils.parseUpdate(NanoHTTPD.readBodyString(session));

        BotFragment fragment = bots.get(path);

        if (fragment == null) {

            new TelegramBot(path).execute(new DeleteWebhook());

            // TODO : 需要吗？

            return null;

        }
        
        fragment.processAsync(update);

        return Response.newFixedLengthResponse("");
    }

}
