package io.kurumi.ntt.fragment;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.HashMap;

public class BotServer {

    public static BotServer INSTANCE;

    public static HashMap<String, BotFragment> fragments = new HashMap<>();
    public static HashMap<Long, BotFragment> idIndex = new HashMap<>();

    public int port;
    public File socketFile;
    public String domain;

    private static Channel server;

    public BotServer(int port, String domain) {
        this.port = port;
        this.domain = domain;
    }

    public BotServer(File socketFile, String domain) {
        this.socketFile = socketFile;
        this.domain = domain;
    }

    public void start() throws Exception {

        stop();

        final EventLoopGroup bossGroup;
        final EventLoopGroup workerGroup;

        if (socketFile != null) {

            bossGroup = new EpollEventLoopGroup();
            workerGroup = new EpollEventLoopGroup(16);

        } else {

            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup(16);

        }

        ServerBootstrap boot = new ServerBootstrap().group(bossGroup, workerGroup);

        if (socketFile != null) {

            boot.channel(EpollServerDomainSocketChannel.class);

        } else {

            boot.channel(NioServerSocketChannel.class);

        }

        boot.option(ChannelOption.SO_BACKLOG, 128);

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

            server = boot.bind(new InetSocketAddress("127.0.0.1", port)).sync().channel();

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

}
