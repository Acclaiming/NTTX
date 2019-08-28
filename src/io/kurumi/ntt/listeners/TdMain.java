package io.kurumi.ntt.listeners;

import io.kurumi.ntt.td.TdApi.*;

import cn.hutool.log.Log;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.listeners.TdMain;
import io.kurumi.ntt.td.client.TdBot;
import io.kurumi.ntt.td.model.TMsg;
import cn.hutool.log.LogFactory;
import io.kurumi.ntt.td.client.TdException;
import cn.hutool.core.util.StrUtil;

public class TdMain extends TdBot {

	public static Log log = LogFactory.get(TdMain.class);

	public TdMain() {

		super(Env.BETA_TOKEN);

	}

	@Override
	public void onNewMessage(final UpdateNewMessage update) {

		if (update.message.senderUserId == me.id) return;

		TMsg msg = new TMsg(this,update.message);

		if (msg.isText()) {

			log.debug("{} : {}",msg.sender,msg.text());

			if ("ping".equals(msg.command())) {

				sendPlainText(msg.chatId,"喵....");
				
			} else if ("test".equals(msg.command())) {
				
				sendPlainText(msg.chatId,"ChatId : {},Sender : {}",msg.chatId,msg.sender);

			}

		}

	}
	
	void sendPlainText(long chatId,String text,java.lang.Object... params) {
		
		send(new SendMessage(chatId,0,true,false,null,plainText(StrUtil.format(text,params))));
		
	}

	InputMessageText plainText(String text) {

		FormattedText format = new FormattedText(text,new TextEntity[0]);

		return new InputMessageText(format,true,true);

	}

}
