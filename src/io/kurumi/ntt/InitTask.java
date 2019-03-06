package io.kurumi.ntt;

import io.kurumi.ntt.utils.*;
import io.kurumi.ntt.db.*;

public class InitTask implements Runnable {

	@Override
	public void run() {
		
		BotLog.debug("开始初始化程序");
		
		UserData.getAll();
		
	}
	
}
