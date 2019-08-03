package io.kurumi.ntt.fragment.mstd.login;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.fragment.mstd.MstdAuth;

public class MsLogout extends Fragment {

	@Override
	public void init(BotFragment origin) {
	
		super.init(origin);
		
		registerFunction("ms_logout");
		
	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
		if (MstdAuth.data.deleteById(user.id)) {
			
			msg.send("已经移除 ~").async();
			
		} else {
			
			msg.send("还没有认证过啦 :)").async();
			
		}
		
	}
	
}
