package io.kurumi.ntt.cqhttp;

import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;

public class TinxHandler extends SimpleChannelInboundHandler<Object> {

    private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;

    public TinxBot bot;

    public TinxHandler(TinxBot bot, WebSocketClientHandshaker handshaker) {

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
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

        Channel ch = ctx.channel();

        if (!handshaker.isHandshakeComplete()) {

            try {

                handshaker.finishHandshake(ch, (FullHttpResponse) msg);

                // BotLog.debug("cqhttp-api 完成握手");
                handshakeFuture.setSuccess();

            } catch (WebSocketHandshakeException e) {

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


            Processer.processUpdate(bot, textFrame.text());

        } else if (frame instanceof PongWebSocketFrame) {
        } else if (frame instanceof CloseWebSocketFrame) {

            ch.close();

        }


    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        cause.printStackTrace();

        if (!handshakeFuture.isDone()) {

            handshakeFuture.setFailure(cause);

        }

        ctx.close();

    }
}
