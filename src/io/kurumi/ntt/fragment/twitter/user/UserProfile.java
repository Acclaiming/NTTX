package io.kurumi.ntt.fragment.twitter.user;

import io.kurumi.ntt.fragment.abs.TwitterFunction;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.abs.Msg;
import java.util.LinkedList;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.archive.UserArchive;
import cn.hutool.core.util.NumberUtil;
import io.kurumi.ntt.utils.NTT;
import java.awt.Color;

public class UserProfile extends TwitterFunction {

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {
		
		if (params.length == 0) {
			
			msg.send("/user <用户ID/用户名/链接>").exec();
			
			return;
			
		}
		
		UserArchive target = NumberUtil.isNumber(params[0]) ? UserArchive.show(account,NumberUtil.parseLong(params[0])) : UserArchive.show(account,NTT.parseScreenName(params[0]));
	
		if (target == null) {
			
			msg.send("找不到用户...").exec();
			
			return;
			
		}
		
		Img img = new Img(1000,600,Color.WHITE);
		
		img.font("Noto Thin");
		
		// sendFile();
		
	}

	@Override
	public void functions(LinkedList<String> names) {
		
		names.add("user");
		
	}

}
