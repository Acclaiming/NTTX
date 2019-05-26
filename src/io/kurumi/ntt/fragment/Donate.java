package io.kurumi.ntt.fragment;

import io.kurumi.ntt.funcs.abs.*;
import java.util.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.*;

public class Donate extends Function {

	@Override
	public void functions(LinkedList<String> names) {
		
		names.add("donate");
		names.add("ccinit");
		
	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
	
		if ("donate".equals(function)) {
			
			
			
			msg.send("感谢。点击下方Url为服务器账户 (位于CloudCone) 氪金。","由于官方限制 支付宝氪金金额至少为 $5 氪金请谨慎 (","戳这里 : https://" + Env.get("server_domain") + "/donate?amout=5").exec();
			
		} else {
		
		if (!user.developer()) {
			
			msg.send("permission denied").exec();
			
			return;
			
		}
		
		if (params.length != 2) {
			
			msg.send("/ccinit <email> <password>").exec();
			
			return;
			
		}
		
		Env.set("donate.cc.email",params[0]);
		Env.set("donate.cc.password",params[1]);
		
		msg.send("successful").exec();
		
		}
		
	}

}
