package io.kurumi.ntt;

import io.kurumi.ntt.utils.*;
import io.kurumi.ntt.db.*;
import java.util.*;
import io.kurumi.ntt.stickers.*;

public class InitTask implements Runnable {

	@Override
	public void run() {
		
		BotLog.debug("开始初始化 ~");
		
		LinkedList<UserData> allUser = UserData.INSTANCE.all();

		for (UserData user : allUser) {
			
			if (!user.refresh(BotMain.INSTANCE)) {
				
				UserData.INSTANCE.delObj(user);
				
				BotLog.debug("用户已停用会话 : " + user.userName() + " @" + user.userName);
				
			}
			
		}
		
		if (!DVANG.INSTANCE.refresh()) {
			
			BotLog.warn("DVANG 表情包 已失效！");
			
		}
		
		if (!索菲.INSTANCE.refresh()) {
			
			BotLog.warn("索菲表情包已失效！");
			
		}
		
		BotLog.info("初始化完成 ~");
		
	}
	
}
