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
	public LinkedList<TinxListener> listeners = new LinkedList<>();

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

                BotLog.debug("cqhttp-api 完成握手");
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

            BotLog.debug("cqhttp-api 收到消息");
			BotLog.debug(textFrame.text());

			Update update = Processer.processUpdate(textFrame.text());

			processUpdate(update);

        } else if (frame instanceof PongWebSocketFrame) {

            BotLog.debug("cqhttp-api 收到心跳");

        } else if (frame instanceof CloseWebSocketFrame) {

			BotLog.info("cqhttp-api 连接关闭");

            ch.close();

        }

    }

	void processUpdate(Update update) {

		for (TinxListener listener : listeners) listener.onUpdate(update);

		if (update instanceof MessageUpdate) {

			MessageUpdate msg = (MessageUpdate) update;

			for (TinxListener listener : listeners) listener.onMsg(msg);

			if (Variants.MSG_PRIVATE.equals(msg.message_type)) {

				for (TinxListener listener : listeners) listener.onPrivate(msg);

			} else if (Variants.MSG_GROUP.equals(msg.message_type)) {

				for (TinxListener listener : listeners) listener.onGroup(msg);

			}

		} else if (update instanceof NoticeUpdate) {

			NoticeUpdate notice = (NoticeUpdate) update;

			for (TinxListener listener : listeners) listener.onNotice(notice);

			if (notice instanceof GroupUploadNotice) {

				GroupUploadNotice upload = (GroupUploadNotice) notice;

				for (TinxListener listener : listeners) listener.onGroupUpload(upload);

			} else if (notice instanceof GroupAdminNotice) {

				GroupAdminNotice admin = (GroupAdminNotice) notice;

				if (Variants.GROUP_ADMIN_SET.equals(admin.sub_type)) {

					for (TinxListener listener : listeners) listener.onGroupAdminSet(admin);

				} else if (Variants.GROUP_ADMIN_UNSET.equals(admin.sub_type)) {

					for (TinxListener listener : listeners) listener.onGroupAdminUnSet(admin);

				}

			} else if (notice instanceof GroupIncreaseNotice) {

				GroupIncreaseNotice inc = (GroupIncreaseNotice) notice;

				for (TinxListener listener : listeners) listener.onGroupIncrease(inc);

				if (Variants.GROUP_INC_INVITE.equals(inc.sub_type)) {

					for (TinxListener listener : listeners) listener.onGroupInviteMember(inc);

				} else if (Variants.GROUP_INC_APPROVE.equals(inc.sub_type)) {

					for (TinxListener listener : listeners) listener.onGroupApproveMember(inc);

				}

			} else if (notice instanceof GroupDecreaseNotice) {

				GroupDecreaseNotice dec = (GroupDecreaseNotice) notice;

				for (TinxListener listener : listeners) listener.onGroupDecrease(dec);

				if (Variants.GROUP_DEC_LEAVE.equals(dec.sub_type)) {

					for (TinxListener listener : listeners) listener.onGroupLeftMember(dec);

				} else if (Variants.GROUP_DEC_KICK.equals(dec.sub_type)) {

					for (TinxListener listener : listeners) listener.onGroupKickMember(dec);

				} else if (Variants.GROUP_DEC_KICK_ME.equals(dec.sub_type)) {

					for (TinxListener listener : listeners) listener.onGroupKickMe(dec);

				}

			} else if (notice instanceof FriendAddNotice) {

				FriendAddNotice add = (FriendAddNotice) notice;

				for (TinxListener listener : listeners) listener.onFriendAdd(add);

			}

		} else if (update instanceof RequestUpdate) {

			RequestUpdate request = (RequestUpdate) update;

			for (TinxListener listener : listeners) listener.onUpdate(request);

			if (request instanceof GroupRequest) {

				GroupRequest group = (GroupRequest) update;

				for (TinxListener listener : listeners) listener.onGroupRequest(group);

				if (Variants.GR_ADD.equals(group.sub_type)) {

					for (TinxListener listener : listeners) listener.onGroupAddRequest(group);

				} else if (Variants.GR_INVITE.equals(group.sub_type)) {

					for (TinxListener listener : listeners) listener.onGroupInviteRequest(group);

				}

			} else if (request instanceof FriendRequest) {

				FriendRequest friend = (FriendRequest) request;

				for (TinxListener listener : listeners) listener.onFriendAddRequest(friend);

			}

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
