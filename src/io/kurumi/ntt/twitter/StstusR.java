package io.kurumi.ntt.twitter;

import twitter4j.*;
import io.kurumi.ntt.db.*;

public class StstusR {
	
	public static boolean exists(Long id) {
		
		return BotDB.exists("data/status",id.toString());
		
	}
	
	public static Status get(Long id) {
		
		return exists(id) ? ObjectUtil.parseStatus(BotDB.get("data/status",id.toString())) : null;

	}
	
	public static void put(Status status) {

		 BotDB.set("data/status",((Long)status.getId()).toString(),ObjectUtil.toString(status));

	}
	
}
