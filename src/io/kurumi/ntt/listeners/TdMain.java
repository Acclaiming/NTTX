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
import io.kurumi.ntt.listeners.base.TdPingFunction;
import io.kurumi.ntt.listeners.base.TdGetIdFunction;

public class TdMain extends TdBot {

	public static Log log = LogFactory.get(TdMain.class);

	public TdMain() {

		super(Env.BETA_TOKEN);
		
		addListener(new TdPingFunction());
		addListener(new TdGetIdFunction());
		
	}

}
