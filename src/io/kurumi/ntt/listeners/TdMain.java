package io.kurumi.ntt.listeners;

import io.kurumi.ntt.td.TdApi.*;

import cn.hutool.log.Log;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.listeners.TdMain;
import io.kurumi.ntt.td.client.TdBot;
import io.kurumi.ntt.td.model.TdMessage;
import cn.hutool.log.LogFactory;

public class TdMain extends TdBot {

	public static Log log = LogFactory.get(TdMain.class);

	public TdMain() {

		super(Env.BETA_TOKEN);

	}

	@Override
	public void onNewMessage(final UpdateNewMessage update) {
		
		if (update.message.senderUserId == me.id) return;

		TdMessage msg = new TdMessage(this,update);

		//if (msg.isText()) {

			log.debug("{} : {}",msg.sender,msg.text());

			//if ("ping".equals(msg.command())) {

				send(new SendMessage(msg.chatId,0,true,false,null,plainText("喵")));

			//}

		//}

	}

	InputMessageText plainText(String text) {

		FormattedText format = new FormattedText(text,new TextEntity[0]);

		return new InputMessageText(format,true,true);

	}

}
