package io.kurumi.ntt.fragment;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.json.JSONObject;
import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.BaseRequest;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.Launcher;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.BotLog;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.stream.ChunkedFile;
import java.io.File;
import javax.activation.MimetypesFileTypeMap;

import static io.netty.handler.codec.http.HttpMethod.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;
import cn.hutool.log.StaticLog;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.DeleteWebhook;
import cn.hutool.core.util.StrUtil;
import io.kurumi.ntt.fragment.twitter.ApiToken;
import twitter4j.User;
import twitter4j.TwitterException;
import io.kurumi.ntt.utils.NTT;
import io.kurumi.ntt.fragment.twitter.TAuth;
import cn.hutool.core.io.FileUtil;
import io.kurumi.ntt.utils.Html;
import javax.print.attribute.standard.NumberUp;
import cn.hutool.core.util.NumberUtil;
import io.kurumi.ntt.fragment.twitter.archive.UserArchive;
import cn.hutool.http.HtmlUtil;
import cn.hutool.core.date.DateUtil;
import java.util.Date;
import cn.hutool.core.util.URLUtil;

public class BotServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

	private String tug_domain = "https://get-twi.me/";

    private FullHttpRequest request;

	String index() {

		return FileUtil.readUtf8String(new File(Env.ROOT_DIR,"res/twi-get/index.html"));

	}

	String error(String message) {

		return StrUtil.format(FileUtil.readUtf8String(new File(Env.ROOT_DIR,"res/twi-get/error.html")),message);

	}

	String result(String message) {

		return StrUtil.format(FileUtil.readUtf8String(new File(Env.ROOT_DIR,"res/twi-get/result.html")),message);

	}

	public void channelRead1(ChannelHandlerContext ctx,FullHttpRequest request) throws Exception {

		String uri = URLUtil.decode(request.uri());

		if (((StrUtil.count(uri,"/") < 2 && !uri.endsWith("/")) && !uri.contains("?screenName="))) {

			sendHtml(ctx,index());

			return;

		} else if (uri.contains("?screenName=")) {

			String screenName = StrUtil.subAfter(uri,"?screenName=",false);

			if (screenName.contains("&")) screenName = StrUtil.subBefore(screenName,"&",false);

			screenName = NTT.parseScreenName(screenName);

			try {

				User user = TAuth.next().createApi().showUser(screenName);

				sendHtml(ctx,result(user.getName() + " 的永久链接是 : " + tug_domain + user.getId()));

				return;

			} catch (TwitterException ex) {

				sendHtml(ctx,error(NTT.parseTwitterException(ex)));

				return;

			}

		} else {

			String userId = StrUtil.subAfter(uri,"/",true);

			if (userId.contains("?")) userId = StrUtil.subBefore(userId,"?",false);

			if (!NumberUtil.isLong(userId)) {

				sendHtml(ctx,result("喵... ？"));

				return;

			}

			try {

				User user = TAuth.next().createApi().showUser(NumberUtil.parseLong(userId));

				UserArchive.save(user);

				sendRedirect(ctx,"https://twitter.com/" + user.getScreenName());

				return;

			} catch (TwitterException ex) {

				String message = "不见了 Σ(ﾟ∀ﾟﾉ)ﾉ<br /><br />";

				message += Html.b(ex.getMessage()) + "<br /><br />";

				message += Html.b("UID") + " : " + userId;

				UserArchive archive = UserArchive.get(NumberUtil.parseLong(userId));

				if (archive != null) {

					message += "<br />" + Html.b("Name") + " : " + HtmlUtil.escape(archive.name);
					message += "<br />" + Html.b("SN") + " : " + Html.twitterUser("@" + archive.screenName,archive.screenName);

					if (!StrUtil.isBlank(archive.bio)) {

						message += "<br /><br />" + Html.b("BIO") + " : " + HtmlUtil.escape(archive.bio) + "<br />";
						
					}
					
					if (archive.followers != null) {
						
						message += "<br />" + archive.following + " Following         " + archive.followers + " Followers<br />";
						
					}
					
					message += "<br />" + "Joined : " + DateUtil.formatDate(new Date(archive.createdAt));


				}

				sendOk(ctx,message);

				return;

			}

		}

	}

    @Override
    public void channelRead0(ChannelHandlerContext ctx,FullHttpRequest request) throws Exception {

        this.request = request;

		// StaticLog.debug("收到HTTP请求 : {}",request.uri());

        if (new File("/etc/ntt/safe").isFile()) {

            sendOk(ctx);

            return;

        }

        if (!request.decoderResult().isSuccess()) {

            sendError(ctx,BAD_REQUEST);

            return;

        }

		if (request.uri().startsWith("/twi-get")) {

			channelRead1(ctx,request);

			return;

		} else if (request.uri().equals("/api")) {

			if (request.getMethod() != POST) {

				sendError(ctx,BAD_REQUEST);

				return;

			}

			JSONObject json;

			try {

				json = new JSONObject(request.content().toString(CharsetUtil.CHARSET_UTF_8));

			} catch (Exception ex) {

				sendError(ctx,BAD_REQUEST);

				return;

			}

			FullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1,OK,Unpooled.copiedBuffer(RpcApi.execute(json).toStringPretty(),CharsetUtil.CHARSET_UTF_8));

            resp.headers().set(HttpHeaderNames.CONTENT_TYPE,"application/json; charset=UTF-8");

            boolean keepAlive = HttpUtil.isKeepAlive(request);

            if (!keepAlive) {

                ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);

            } else {

                resp.headers().set(HttpHeaderNames.CONNECTION,HttpHeaderValues.KEEP_ALIVE);

                ctx.writeAndFlush(resp);

            }

			return;

		}

        if (Launcher.INSTANCE != null && request.uri().equals("/data/" + Launcher.INSTANCE.getToken())) {

            File dataFile = new File(Env.CACHE_DIR,"data.zip");

            if (!dataFile.isFile()) {

                sendError(ctx,NOT_FOUND);

                return;

            }

            FullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1,OK);

            MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();

            resp.headers().set(HttpHeaderNames.CONTENT_TYPE,mimeTypesMap.getContentType(dataFile));
            resp.headers().set(HttpHeaderNames.CONTENT_LENGTH,dataFile.length());

            final boolean keepAlive = HttpUtil.isKeepAlive(request);

            if (!keepAlive) {

                resp.headers().set(HttpHeaderNames.CONNECTION,HttpHeaderValues.CLOSE);

            } else if (request.protocolVersion().equals(HTTP_1_0)) {

                resp.headers().set(HttpHeaderNames.CONNECTION,HttpHeaderValues.KEEP_ALIVE);

            }

            ctx.write(resp);

            ChannelFuture last = ctx.writeAndFlush(new ChunkedFile(dataFile)).addListener(ChannelFutureListener.CLOSE);

            if (!keepAlive) {

                last.addListener(ChannelFutureListener.CLOSE);

            }

            return;

        } else if (Launcher.INSTANCE != null && request.uri().equals("/upgrade/" + Launcher.INSTANCE.getToken())) {

            try {

                JSONObject json = new JSONObject(request.content().toString(CharsetUtil.CHARSET_UTF_8));

                if (!"refs/heads/master".equals(json.getStr("ref"))) {

                    sendOk(ctx);

                    return;

                }

            } catch (Exception ex) {
            }

            sendOk(ctx);

            new Thread() {

                @Override
                public void run() {

                    new Send(Env.LOG_CHANNEL,"Bot Update Executed : By WebHook").exec();

                    try {

                        String str = RuntimeUtil.execForStr("bash update.sh");

                        new Send(Env.LOG_CHANNEL,str).exec();

                        Launcher.INSTANCE.stop();

                    } catch (Exception e) {

                        new Send(Env.LOG_CHANNEL,BotLog.parseError(e)).exec();

                    }

                    RuntimeUtil.exec("service mongod restart");

                    RuntimeUtil.exec("service ntt restart");

                }


            }.start();

            return;

        } else if (request.getMethod() != POST || request.uri().length() <= 1) {

            sendError(ctx,NOT_FOUND);

            return;

        }

        String botToken = request.uri().substring(1);

        if (!BotServer.fragments.containsKey(botToken)) {

			// StaticLog.debug("未预期的消息 : {}",request.content().toString(CharsetUtil.CHARSET_UTF_8));

			FullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1,OK,Unpooled.copiedBuffer(new DeleteWebhook().toWebhookResponse(),CharsetUtil.CHARSET_UTF_8));

            resp.headers().set(HttpHeaderNames.CONTENT_TYPE,"application/json; charset=UTF-8");

			ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);

            return;

        }

        BaseRequest webhookResponse;

		ProcessLock<BaseRequest> lock = new ProcessLock<>();

		Update update = BotUtils.parseUpdate(request.content().toString(CharsetUtil.CHARSET_UTF_8));

		update.lock = lock;

        try {

			BotServer.fragments.get(botToken).processAsync(update);

			webhookResponse = lock.waitFor();

        } catch (Exception ex) {

			StaticLog.error("出错 (同步) \n\n{}\n\n{}",new JSONObject(update.json).toStringPretty(),BotLog.parseError(ex));

            webhookResponse = null;

            //sendError(ctx,INTERNAL_SERVER_ERROR);

        }

		if (webhookResponse == null) {

			sendOk(ctx);

        } else {

            // System.out.println(webhookResponse.toWebhookResponse());

            FullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1,OK,Unpooled.copiedBuffer(webhookResponse.toWebhookResponse(),CharsetUtil.CHARSET_UTF_8));

            resp.headers().set(HttpHeaderNames.CONTENT_TYPE,"application/json; charset=UTF-8");

            boolean keepAlive = HttpUtil.isKeepAlive(request);

            if (!keepAlive) {

                ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);

            } else {

                resp.headers().set(HttpHeaderNames.CONNECTION,HttpHeaderValues.KEEP_ALIVE);

                ctx.writeAndFlush(resp);

            }

        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause) {

        cause.printStackTrace();

        if (ctx.channel().isActive()) {

            sendError(ctx,INTERNAL_SERVER_ERROR);

        }

    }

    void sendOk(ChannelHandlerContext ctx) {

        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,OK);

        this.sendAndCleanupConnection(ctx,response);

    }

	void sendOk(ChannelHandlerContext ctx,String content) {

		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,OK,Unpooled.copiedBuffer(content,CharsetUtil.CHARSET_UTF_8));

		response.headers().set(HttpHeaderNames.CONTENT_TYPE,"application/json; charset=UTF-8");

		this.sendAndCleanupConnection(ctx,response);

	}

	void sendHtml(ChannelHandlerContext ctx,String content) {

		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,OK,Unpooled.copiedBuffer(content,CharsetUtil.CHARSET_UTF_8));

		response.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/html; charset=UTF-8");

		this.sendAndCleanupConnection(ctx,response);

	}

    void sendRedirect(ChannelHandlerContext ctx,String newUri) {

        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,FOUND);

        response.headers().set(HttpHeaderNames.LOCATION,newUri);

        this.sendAndCleanupConnection(ctx,response);

    }

    void sendError(ChannelHandlerContext ctx,HttpResponseStatus status) {

		FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,status);

        this.sendAndCleanupConnection(ctx,response);


    }

	void sendError(ChannelHandlerContext ctx,HttpResponseStatus status,String content) {

        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,status,Unpooled.copiedBuffer(content,CharsetUtil.CHARSET_UTF_8));

        response.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/plain; charset=UTF-8");

        this.sendAndCleanupConnection(ctx,response);

    }

    void sendAndCleanupConnection(ChannelHandlerContext ctx,FullHttpResponse response) {

        final FullHttpRequest request = this.request;

        final boolean keepAlive = HttpUtil.isKeepAlive(request);

        HttpUtil.setContentLength(response,response.content().readableBytes());

        if (!keepAlive) {

            // We're going to close the connection as soon as the response is sent,
            // so we should also make it clear for the client.

            response.headers().set(HttpHeaderNames.CONNECTION,HttpHeaderValues.CLOSE);

        } else if (request.protocolVersion().equals(HTTP_1_0)) {

            response.headers().set(HttpHeaderNames.CONNECTION,HttpHeaderValues.KEEP_ALIVE);

        }

        ChannelFuture flushPromise = ctx.writeAndFlush(response);

        if (!keepAlive) {

            // Close the connection as soon as the response is sent.

            flushPromise.addListener(ChannelFutureListener.CLOSE);

        }
    }

}
