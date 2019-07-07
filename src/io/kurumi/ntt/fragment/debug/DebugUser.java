package io.kurumi.ntt.fragment.debug;

import cn.hutool.core.util.NumberUtil;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.NTT;
import twitter4j.TwitterException;

public class DebugUser extends Fragment {

	@Override
	public void init(BotFragment origin) {
	
		super.init(origin);
		
		registerFunction("get_user");
		
	}
	
	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
		if (params.length == 0) {

            msg.send("invaild user").exec();

            return;

        }
		
		requestTwitter(user,msg);
		
	}
	
    @Override
    public void onTwitterFunction(UserData user, Msg msg, String function, String[] params, TAuth account) {

        if (NumberUtil.isNumber(params[0])) {

            try {

                msg.send(account.createApi().showUser(NumberUtil.parseLong(params[0])).toString()).exec();

            } catch (TwitterException e) {

                msg.send(NTT.parseTwitterException(e)).exec();

            }

        } else {

            try {

                msg.send(account.createApi().showUser(NTT.parseScreenName(params[0])).toString()).exec();

            } catch (TwitterException e) {

                msg.send(NTT.parseTwitterException(e)).exec();

            }

        }

    }

}
