package io.kurumi.ntt.fragment.twitter.ext;

import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.utils.Html;

public class AuthExport extends Fragment {

	@Override
	public void init(BotFragment origin) {
	
		super.init(origin);
		
		registerFunction("auth");
		
	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
		requestTwitter(user,msg);
		
	}

	@Override
	public void onTwitterFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {
		
		StringBuilder auth = new StringBuilder("认证信息 :");
		
		auth.append("\nID : ").append(Html.code(account.id));
		auth.append("\nApiKey : ").append(Html.code(account.apiKey));
		auth.append("\nApiKeySec : ").append(Html.code(account.apiKeySec));
		auth.append("\nAccToken : ").append(Html.code(account.accToken));
		auth.append("\nAccTokenSec : ").append(Html.code(account.accTokenSec));
		
		msg.send(auth.toString()).html().exec();
		
	}
	
}
