package io.kurumi.ntt.fragment;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.http.HttpUtil;
import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.DeleteWebhook;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.Launcher;
import io.kurumi.ntt.fragment.BotServer;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.BotLog;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.stream.ChunkedWriteHandler;
import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import javax.activation.MimetypesFileTypeMap;
import io.netty.channel.ChannelOption;

import static io.netty.handler.codec.http.HttpMethod.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;
import io.netty.channel.ServerChannel;


public class NettyServer extends SimpleChannelInboundHandler<FullHttpRequest> {

	public static NettyServer INSTANCE;
	
	public int port;
	public File socketFile;
    public  String domain;

	private static Channel server;

	public NettyServer(int port,String domain) {
		this.port = port;
		this.domain = domain;
	}

	public NettyServer(File socketFile,String domain) {
		this.socketFile = socketFile;
		this.domain = domain;
	}

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause) { 

        cause.printStackTrace();
        ctx.close();

    }

	public void start() throws Exception {

		stop();

		final EventLoopGroup bossGroup;
		final EventLoopGroup workerGroup;

		if (socketFile != null) {

			bossGroup = new EpollEventLoopGroup(1); 
			workerGroup = new EpollEventLoopGroup();

		} else {

			bossGroup = new NioEventLoopGroup(1); 
			workerGroup = new NioEventLoopGroup();

		}

		ServerBootstrap boot = new ServerBootstrap();

		boot.
			group(bossGroup,workerGroup)
			.channel((Class<ServerChannel>)(socketFile != null ? EpollServerDomainSocketChannel.class : NioServerSocketChannel.class))
			.option(ChannelOption.SO_BACKLOG,128)
			.option(ChannelOption.SO_REUSEADDR,true)
			.option(ChannelOption.SO_KEEPALIVE,false)
			.childOption(ChannelOption.TCP_NODELAY,true)
			.childHandler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel ch) throws Exception {

					ChannelPipeline pipeline = ch.pipeline();

					pipeline.addLast(new HttpServerCodec());
					pipeline.addLast(new HttpObjectAggregator(65536));
					pipeline.addLast(new ChunkedWriteHandler());
					pipeline.addLast(NettyServer.this);

				}


			});


		if (socketFile != null) {

			server = boot.bind(new DomainSocketAddress(socketFile)).sync().channel();

		} else {

			server = boot.bind(port).sync().channel();

		}

		new Thread() {

			@Override
			public void run() {

				try {

					server.closeFuture().sync();

				} catch (InterruptedException e) {
				} finally {

					bossGroup.shutdownGracefully();
					workerGroup.shutdownGracefully();

				}
			}


		}.start();

	}

	public static void stop() {

		if (server != null) {

			server.close();

			server = null;

		}

	}

	private FullHttpRequest request;

	@Override
	protected void channelRead0(ChannelHandlerContext ctx,FullHttpRequest request) throws Exception {

		try {

			this.request = request;

			if (!request.decoderResult().isSuccess()) {

				sendError(ctx,BAD_REQUEST);

				return;

			}

			if (request.uri().equals("/data/" + Launcher.INSTANCE.getToken())) {

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

			} else if (request.uri().equals("/upgrade/" + Launcher.INSTANCE.getToken())) {

				sendOk(ctx);

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

				return;

			} else 

			if (request.getMethod() != POST || request.uri().length() <= 1) {

				sendError(ctx,NOT_FOUND);

				return;

			}

			String botToken = request.uri().substring(1);

			if (!fragments.containsKey(botToken)) {

				sendError(ctx,INTERNAL_SERVER_ERROR);

				new TelegramBot(botToken).execute(new DeleteWebhook());

				return;

			}


			Update update = BotUtils.parseUpdate(request.content().toString(CharsetUtil.CHARSET_UTF_8));

			update.lock = new ProcessLock();

			sendOk(ctx);

			BaseRequest webhookResponse = fragments.get(botToken).processAsync(update);

			if (webhookResponse == null) {

				sendOk(ctx);

			} else 	{

				FullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1,OK,stringByteBuf(webhookResponse.toWebhookResponse()));

				resp.headers().set(HttpHeaderNames.CONTENT_TYPE,"application/json; charset=UTF-8");

				boolean keepAlive = HttpUtil.isKeepAlive(request);

				if (!keepAlive) {

					ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);

				} else {

					resp.headers().set(HttpHeaderNames.CONNECTION,HttpHeaderValues.KEEP_ALIVE);

					ctx.writeAndFlush(resp);

				}

			}

		} catch (Exception ex) {

			ex.printStackTrace();

		}

	}

	public static class ProcessLock extends ReentrantLock {

		private Condition condition = newCondition();
		private BaseRequest request;
		public AtomicBoolean used = new AtomicBoolean(false);

		public BaseRequest waitFor() throws InterruptedException {

			lockInterruptibly();

			try {

				this.condition.await(1000,TimeUnit.MILLISECONDS);

				if (request != null) System.out.println(request.toWebhookResponse());

				return request;

			} finally {

				unlock();

			}

		}

		public void unlock(BaseRequest request) {

			lock();

			this.request = request;

			try {

				this.condition.signalAll();

			} finally {

				unlock();

			}

		}

	}

	ByteBuf stringByteBuf(String text) {

		return Unpooled.copiedBuffer(text,CharsetUtil.CHARSET_UTF_8);

	}

	void sendOk(ChannelHandlerContext ctx) {

		FullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1,OK);

		resp.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/plain; charset=UTF-8");

		sendAndCleanupConnection(ctx,resp);

	}

	void sendError(ChannelHandlerContext ctx,HttpResponseStatus status) {

		FullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1,status,stringByteBuf("ERROR"));

		resp.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/plain; charset=UTF-8");

		sendAndCleanupConnection(ctx,resp);

	}

	void sendRedirect(ChannelHandlerContext ctx,String redirectTo) {

		FullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1,FOUND);

		resp.headers().set(HttpHeaderNames.LOCATION,redirectTo);

		sendAndCleanupConnection(ctx,resp);

	}

	void sendAndCleanupConnection(ChannelHandlerContext ctx,FullHttpResponse response) {

		final boolean keepAlive = HttpUtil.isKeepAlive(request);

		HttpUtil.setContentLength(response,response.content().readableBytes());

		if (!keepAlive) {

			response.headers().set(HttpHeaderNames.CONNECTION,HttpHeaderValues.CLOSE);

		} else if (request.protocolVersion().equals(HTTP_1_0)) {

			response.headers().set(HttpHeaderNames.CONNECTION,HttpHeaderValues.KEEP_ALIVE);

		}

		ChannelFuture flushPromise = ctx.writeAndFlush(response);

		if (!keepAlive) {

			flushPromise.addListener(ChannelFutureListener.CLOSE);

		}

	}


}
