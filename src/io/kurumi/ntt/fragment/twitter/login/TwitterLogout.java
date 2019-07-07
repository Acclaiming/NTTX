package io.kurumi.ntt.fragment.twitter.login;

import io.kurumi.ntt.Env;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.Send;

public class TwitterLogout extends Fragment {

	@Override
	public void init(BotFragment origin) {
	
		super.init(origin);
		
		registerFunction("logout");
		
	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
		requestTwitter(user,msg,true);
		
	}
	
    @Override
    public void onTwitterFunction(UserData user, Msg msg, String function, String[] params, TAuth account) {

        TAuth.data.deleteById(account.id);

        msg.send("乃的授权 " + account.archive().urlHtml() + "已移除 ~").html().exec();

        new Send(Env.GROUP, "Removed Auth : " + user.userName() + " -> " + account.archive().urlHtml()).html().exec();

    }

}
