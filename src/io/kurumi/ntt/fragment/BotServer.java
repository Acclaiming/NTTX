package io.kurumi.ntt.fragment;

import com.pengrad.telegrambot.request.BaseRequest;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import java.io.File;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static io.netty.handler.codec.http.HttpMethod.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

public class BotServer {

	public static BotServer INSTANCE;

	public static HashMap<String, BotFragment> fragments = new HashMap<>();

	public int port;
	public File socketFile;
    public  String domain;

	private static Channel server;

	public BotServer(int port,String domain) {
		this.port = port;
		this.domain = domain;
	}

	public BotServer(File socketFile,String domain) {
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

		ServerBootstrap boot = new ServerBootstrap().group(bossGroup,workerGroup);

		if (socketFile != null) {

			boot.channel(EpollServerDomainSocketChannel.class);

		} else {

			boot.channel(NioServerSocketChannel.class);

		}

		boot.option(ChannelOption.SO_BACKLOG,15);

		boot.childHandler(new ChannelInitializer<Channel>() {

				@Override
				protected void initChannel(Channel ch) throws Exception {

					ChannelPipeline pipeline = ch.pipeline();

					pipeline.addLast(new HttpServerCodec());
					pipeline.addLast(new HttpObjectAggregator(65536));
					pipeline.addLast(new ChunkedWriteHandler());
					pipeline.addLast(new BotServerHandler());

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
