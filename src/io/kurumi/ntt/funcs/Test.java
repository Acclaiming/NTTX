package io.kurumi.ntt.funcs;
import io.kurumi.ntt.funcs.abs.*;
import java.util.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.utils.*;
import cn.hutool.core.util.*;

public class Test extends Function {

	public static Test INSTANCE = new Test();
	
	@Override
	public void functions(LinkedList<String> names) {
		
		names.add("test");
		
	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		msg.send("stat").exec();
		
		long[] ids = NTT.getChatMembers(msg.chatId());

		if (ids != null) {
			
			msg.send(ArrayUtil.join(ids,"\n")).exec();
			
		} else {
			
			msg.send("failed").exec();
			
		}
		
	}
	
}
