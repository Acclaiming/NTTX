package io.kurumi.ntt.fragment;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.DeleteWebhook;
import fi.iki.elonen.NanoHTTPD;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.Launcher;
import io.kurumi.ntt.fragment.abs.request.Send;
import io.kurumi.ntt.utils.BotLog;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import cn.hutool.json.JSONObject;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;

public class BotServer extends NanoHTTPD {

    public static HashMap<String, BotFragment> fragments = new HashMap<>();
    public static BotServer INSTANCE;
    public String domain;

    public BotServer(int port,String domain) {

        super("127.0.0.1",port);

        this.domain = domain;

    }

    public String readBody(IHTTPSession session) {

        int contentLength = Integer.parseInt(session.getHeaders().get("content-length"));
        byte[] buf = new byte[contentLength];

        try {
            session.getInputStream().read(buf,0,contentLength);

            return StrUtil.utf8Str(buf);

        } catch (IOException e2) {
        }

        return null;

    }


    @Override
    public Response serve(IHTTPSession session) {

        URL url = URLUtil.url(session.getUri());

        if (url.getPath().equals("/favicon.ico")) {

            return redirectTo("https://kurumi.io/favicon.ico");

        }

        if (url.getPath().startsWith("/data/" + Launcher.INSTANCE.getToken())) {

			return newChunkedResponse(Response.Status.OK,"application/octet-stream",IoUtil.toStream(new File(Env.CACHE_DIR,"data.zip")));

        }
		
		if (url.getPath().startsWith("/send/")) {

			String botToken = StrUtil.subAfter(url.getPath(),"/send/",true);

			if (!fragments.containsKey(botToken)) {

				return newFixedLengthResponse(Response.Status.NOT_FOUND,"plain/text","NO EXISTS BOT SETTED");

			} else {

				try {

					JSONObject request = new JSONObject(readBody(session));
					
					if (!request.containsKey("chat")) {
						
						return newFixedLengthResponse(Response.Status.BAD_REQUEST,"plain/text","NO CHAT SETTED");
						
					} else if (!request.containsKey("text")) {

						return newFixedLengthResponse(Response.Status.BAD_REQUEST,"plain/text","NO TEXT SETTED");

					}
					
					Send send = new Send(request.getLong("chat"),request.getStr("text"));

					if (request.containsKey("html")) send.html();
				
					SendResponse resp = send.exec();

					return newFixedLengthResponse(Response.Status.OK,"plain/text",resp.json == null ? resp.toString() : resp.json);
					
				} catch (Exception e) {
					
					return newFixedLengthResponse(Response.Status.INTERNAL_ERROR,"plain/text",BotLog.parseError(e));
					
				}

			}

		}

		if (url.getPath().startsWith("/upgrade/" + Launcher.INSTANCE.getToken())) {

			new Thread() {

				@Override
				public void run() {

					new Send(Env.GROUP,"Bot Update Executed : By WebHook").exec();

					// Launcher.INSTANCE.stop();

					try {

						String str = RuntimeUtil.execForStr("bash update.sh");

						new Send(Env.GROUP,"update successful , now restarting...\n",str).exec();

						RuntimeUtil.exec("service ntt restart");

					} catch (Exception e) {

						new Send(Env.GROUP,"update failed",BotLog.parseError(e)).exec();

					}

				}

			}.start();

			return newFixedLengthResponse(Response.Status.OK,"plain/text","");

        }

        String botToken = url.getPath().substring(1);

        if (fragments.containsKey(botToken)) {

            try {

                fragments.get(botToken).processAsync(BotUtils.parseUpdate(readBody(session)));

            } catch (Exception ex) {
            }

            return newFixedLengthResponse("");

        } else {

            try {

                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR,MIME_PLAINTEXT,"ERROR");

            } finally {

                new TelegramBot(botToken).execute(new DeleteWebhook());

            }

        }


    }

    public Response redirectTo(String url) {

        Response resp = newFixedLengthResponse(Response.Status.REDIRECT_SEE_OTHER,MIME_HTML,"<html><head><titile>Redirecting...</title></head><body></body></html>");

        resp.addHeader("Location",url);

        return resp;

    }

}
