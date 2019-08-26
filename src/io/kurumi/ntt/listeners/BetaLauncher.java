package io.kurumi.ntt.listeners;

import cn.hutool.core.util.StrUtil;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.td.TdApi.*;
import io.kurumi.ntt.td.client.TdBot;
import io.kurumi.ntt.td.model.TdMessage;
import cn.hutool.log.StaticLog;

public class BetaLauncher extends TdBot {

	public BetaLauncher() {

		super(Env.BETA_TOKEN);

	}

	@Override
	public void onNewMessage(UpdateNewMessage update) {

		StaticLog.debug("new message {}",update);

		TdMessage msg = new TdMessage(this,update);

		// send(msg.send(plainText("喵")));

	}

	InputMessageText plainText(String text) {

		FormattedText format = new FormattedText(text,new TextEntity[0]);

		return new InputMessageText(format,true,true);

	}

}
