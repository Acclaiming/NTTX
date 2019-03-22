package io.kurumi.ntt.funcs;

import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.*;

public class PingFunc extends Fragment {

	public static PingFunc INSTANCE = new PingFunc();
	
	@Override
	public boolean onMsg(UserData user,Msg msg) {
		
		if (!"ping".equals(msg.commandName())) return false;
		
		long start = System.currentTimeMillis();
		
		Msg pongMsg = msg.send("pong！").send();

		long stop = System.currentTimeMillis();
		
		pongMsg.edit("pong！","time : " + (stop - start) + "ms").exec();
		
		return true;
		
	}
	
}
