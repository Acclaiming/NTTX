package io.kurumi.ntt.cqhttp;

import cn.hutool.log.StaticLog;
import io.kurumi.ntt.Launcher;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import io.kurumi.ntt.cqhttp.model.LoginInfo;
import io.kurumi.ntt.cqhttp.response.GetLoginInfoResponse;
import cn.hutool.core.exceptions.ExceptionUtil;

public final class TinxBot {

	private AtomicBoolean stopped = new AtomicBoolean(true);

	private URI uri;
	private Channel client;

	public TinxApi api;
	
	public LoginInfo me;

	public LinkedList<TinxListener> listeners = new LinkedList<>();

	public TinxBot(String wsUrl,String httpUrl) {

		try {

			this.uri = new URI(wsUrl);

		} catch (URISyntaxException e) {

			throw new RuntimeException(e);

		}

		this.api = new TinxApi(httpUrl);

	}

	public void addListener(TinxListener listener) {

		listener.bot = this;
		listener.api = this.api;

		this.listeners.add(listener);

	}

	public void removeListeners() {

		this.listeners.clear();

	}
	
	public boolean send(String json) {
		
		if (client == null || stopped.get()) return false;
		
		WebSocketFrame frame = new TextWebSocketFrame(json);
		
		client.writeAndFlush(frame);
		
		return true;
		
	}

    public void start() throws Exception {

		GetLoginInfoResponse info = api.getLoginInfo();

		if (info.data != null) {
			
			me = info.data;
			
		} else {
			
			throw new IllegalStateException("HTTP API 无法连接 / 未登录");
			
		}
		
        final EventLoopGroup group = new NioEventLoopGroup();

		final Bootstrap boot = new Bootstrap();

		final TinxHandler handler = new TinxHandler(TinxBot.this,WebSocketClientHandshakerFactory.newHandshaker(uri,WebSocketVersion.V13,null,true,new DefaultHttpHeaders()));

		boot.group(group);
		boot.channel(NioSocketChannel.class);
		boot.handler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel ch) {

					ChannelPipeline pipeline = ch.pipeline();

					pipeline.addLast(new HttpClientCodec());
					pipeline.addLast(new HttpObjectAggregator(8192));
					pipeline.addLast(WebSocketClientCompressionHandler.INSTANCE);
					pipeline.addLast(handler);

				}
			});

		try {

			client = boot.connect(uri.getHost(),uri.getPort()).sync().channel();

			handler.handshakeFuture().sync();

		} catch (InterruptedException e) {

			return;

		}
		
		stopped.set(false);
		
		StaticLog.info("CqHttp WebSocket 已连接");

		new Thread("CqHttp Ping Thread") {

			@Override
			public void run() {

				while (client.isActive()) {

					WebSocketFrame frame = new PingWebSocketFrame(Unpooled.wrappedBuffer(new byte[] { 8, 1, 8, 1 }));

					client.writeAndFlush(frame);

					try {

						Thread.sleep(60 * 1000L);

					} catch (InterruptedException e) {

						return;

					}

				}

			}

		}.start();


		new Thread("CqHttp Ws Thread") {

			@Override
			public void run() {

				try {

					client.closeFuture().sync();

				} catch (Exception e) {
				} finally {

					group.shutdownGracefully();

				}

				if (!stopped.get()) {

					Launcher.tryTinxConnect();

				}

			}

		}.start();

    }

	public void stop() {

		if (client != null) {

			client.close();

			client = null;

		}

	}

}
