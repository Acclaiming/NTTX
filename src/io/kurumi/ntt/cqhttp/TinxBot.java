package io.kurumi.ntt.cqhttp;

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
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import java.net.URI;
import java.net.URISyntaxException;
import cn.hutool.core.thread.ThreadUtil;
import java.util.LinkedList;

public final class TinxBot {

	private URI uri;
	private Channel client;
	public TinxApi api;
	public TinxHandler handler;

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

    public void start() throws Exception {

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
					pipeline.addLast(new TinxHandler(TinxBot.this,WebSocketClientHandshakerFactory.newHandshaker(uri,WebSocketVersion.V13,null,true,new DefaultHttpHeaders())));


				}
			});

		try {

			client = boot.connect(uri.getHost(),uri.getPort()).sync().channel();
			
			handler.handshakeFuture().sync();

		} catch (InterruptedException e) {

			return;

		}
		
		new Thread("CqHttp Ping Thread") {

			@Override
			public void run() {

				while (client.isActive()) {

					WebSocketFrame frame = new PingWebSocketFrame(Unpooled.wrappedBuffer(new byte[] { 8, 1, 8, 1 }));
                   
					client.writeAndFlush(frame);
					
					ThreadUtil.safeSleep(60 * 1000L);
					
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
				
				Launcher.tryTinxConnect();

			}

		}.start();



    }

}
