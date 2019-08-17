package io.kurumi.ntt.cqhttp;

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
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import io.netty.channel.ChannelFuture;
import org.apache.http.client.utils.URIUtils;

public final class TinxBot {

	private URI uri;
	private Channel client;
	public TinxApi api;
	public TinxHandler handler;

	public TinxBot(String wsUrl,String httpUrl) {

		try {

			this.uri = new URI(wsUrl);

		} catch (URISyntaxException e) {

			throw new RuntimeException(e);

		}

		this.api = new TinxApi(httpUrl);
		this.handler = new TinxHandler(this,WebSocketClientHandshakerFactory.newHandshaker(uri,WebSocketVersion.V13,null,true,new DefaultHttpHeaders()));

	}

	public void addListener(TinxListener listener) {

		this.handler.listeners.add(listener);

	}

	public void removeListeners() {

		this.handler.listeners.clear();

	}

    public void start() {

        final EventLoopGroup group = new NioEventLoopGroup();

		final Bootstrap boot = new Bootstrap();

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

		new Thread("CqHttp Ws Thread") {

			@Override
			public void run() {

				try {

					client = boot.connect(uri.getHost(),uri.getPort()).sync().channel();

					handler.handshakeFuture().sync();
					
					client.closeFuture().sync();

				} catch (InterruptedException e) {
				} finally {

					group.shutdownGracefully();


				}

			}

		}.start();



    }

}
