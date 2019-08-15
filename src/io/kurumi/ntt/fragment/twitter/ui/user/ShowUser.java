package io.kurumi.ntt.fragment.twitter.ui.user;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.db.UserData;
import cn.hutool.core.util.NumberUtil;
import io.kurumi.ntt.fragment.twitter.archive.UserArchive;

public class ShowUser extends Fragment {

	@Override
	public void init(BotFragment origin) {
		
		super.init(origin);
		
		registerPayload("user");
		
	}

	@Override
	public void onPayload(UserData user,Msg msg,String payload,String[] params) {
		
		if (params.length < 1 || NumberUtil.isNumber(params[0])) {
			
			msg.invalidQuery();
			
			return;
			
		}
		
	}
	
	static String formatUser(UserArchive user) {
		
		String message = "";
		
		return message;
		
	}
	
}
