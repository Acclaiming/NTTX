package io.kurumi.ntt.cqhttp;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import io.netty.util.CharsetUtil;
import io.kurumi.ntt.cqhttp.Processer;
import io.kurumi.ntt.utils.BotLog;
import io.kurumi.ntt.cqhttp.update.Update;
import java.util.LinkedList;
import io.kurumi.ntt.cqhttp.update.MessageUpdate;
import io.kurumi.ntt.cqhttp.update.NoticeUpdate;
import io.kurumi.ntt.cqhttp.update.GroupUploadNotice;
import io.kurumi.ntt.cqhttp.update.GroupAdminNotice;
import io.kurumi.ntt.cqhttp.update.GroupIncreaseNotice;
import io.kurumi.ntt.cqhttp.update.GroupDecreaseNotice;
import io.kurumi.ntt.cqhttp.update.FriendAddNotice;
import io.kurumi.ntt.cqhttp.update.RequestUpdate;
import io.kurumi.ntt.fragment.spam.GroupReport;
import io.kurumi.ntt.cqhttp.update.GroupRequest;
import io.kurumi.ntt.cqhttp.update.FriendRequest;

public class TinxHandler extends SimpleChannelInboundHandler<Object> {

    private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;

	public TinxBot bot;

    public TinxHandler(TinxBot bot,WebSocketClientHandshaker handshaker) {

        this.handshaker = handshaker;
		this.bot = bot;

    }

    public ChannelFuture handshakeFuture() {

        return handshakeFuture;

    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {

        handshakeFuture = ctx.newPromise();

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        handshaker.handshake(ctx.channel());

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

		BotLog.debug("cqhttp-api 已连接");

    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx,Object msg) throws Exception {

        Channel ch = ctx.channel();

        if (!handshaker.isHandshakeComplete()) {

            try {
                handshaker.finishHandshake(ch,(FullHttpResponse) msg);

               // BotLog.debug("cqhttp-api 完成握手");
                handshakeFuture.setSuccess();

            } catch (WebSocketHandshakeException e) {

				BotLog.info("cqhttp-api 握手失败",e);

                handshakeFuture.setFailure(e);
            }

            return;

        }

        if (msg instanceof FullHttpResponse) {

            FullHttpResponse response = (FullHttpResponse) msg;

            throw new IllegalStateException("Unexpected FullHttpResponse (getStatus=" + response.status() + ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');

        }

        WebSocketFrame frame = (WebSocketFrame) msg;

        if (frame instanceof TextWebSocketFrame) {

            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;

            
			Processer.processUpdate(bot,textFrame.text());

        } else if (frame instanceof PongWebSocketFrame) {

            // BotLog.debug("cqhttp-api 收到心跳");

        } else if (frame instanceof CloseWebSocketFrame) {

			BotLog.info("cqhttp-api 连接关闭");

            ch.close();

        }


	}

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause) {

		cause.printStackTrace();

        if (!handshakeFuture.isDone()) {

            handshakeFuture.setFailure(cause);

        }

        ctx.close();

    }
}
