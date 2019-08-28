package io.kurumi.ntt.listeners;

import io.kurumi.ntt.td.TdApi.*;

import cn.hutool.log.Log;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.listeners.TdMain;
import io.kurumi.ntt.td.client.TdBot;
import io.kurumi.ntt.td.model.TMessage;
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

		TMessage msg = new TMessage(this,update);

		if (msg.isText()) {

			log.debug("{} : {}",msg.sender,msg.text());

			if ("ping".equals(msg.command())) {

				send(new SendMessage(msg.chatId,0,true,false,null,plainText("喵")));

			} else if ("test".equals(msg.command())) {
				
				long start = System.currentTimeMillis();
				
				try {
					
					User user = execute(new GetUser(msg.sender));

					sendPlainText(msg.chatId,"完成 : {}ms",(System.currentTimeMillis() - start));
					
				} catch (TdException e) {
					
					sendPlainText(msg.chatId,e.getMessage());
					
				}

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
