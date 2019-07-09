package io.kurumi.ntt.fragment;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.RuntimeUtil;
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
import io.netty.channel.ChannelOption;
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
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.stream.ChunkedWriteHandler;
import java.io.File;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import javax.activation.MimetypesFileTypeMap;

import static io.netty.handler.codec.http.HttpMethod.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

public class NettyServer {

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
			.channel((Class)(socketFile != null ? EpollServerDomainSocketChannel.class : NioServerSocketChannel.class))
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
					pipeline.addLast(new BotServerHandler());
					 //pipeline.addLast(new HttpStaticFileServerHandler());

				}


			});


		if (socketFile != null) {

			server = boot.bind(new DomainSocketAddress(socketFile)).sync().channel();

		} else {

			server = boot.bind(new InetSocketAddress("0.0.0.0",11222)).sync().channel();

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

}
