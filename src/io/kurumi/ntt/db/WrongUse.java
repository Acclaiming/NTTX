package io.kurumi.ntt.db;

import io.kurumi.ntt.utils.BotLog;
import java.util.concurrent.atomic.*;

public class WrongUse {

    public static final String KEY = "NTT_WU";

	public static AtomicLong wu;
	
    public static String incrWithMsg(UserData user) {

        BotLog.debug(user.name() + " 又用错了一次！");

        return "你已经用错 " + incr(user) + " 次了！ （￣～￣)";

    }

    public static Long incr(UserData user) {

        BotLog.debug(user.name() + " 又用错了一次！");

        user.increment("w");
		
		user.save();
		
		return user.getLong("w",1L);

    }

    public static Long get(UserData user) {

        return user.getLong("w",0L);

    }

}
