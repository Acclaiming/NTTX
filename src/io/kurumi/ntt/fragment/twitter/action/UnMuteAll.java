package io.kurumi.ntt.fragment.twitter.action;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.TApi;
import twitter4j.TwitterException;
import twitter4j.Twitter;
import io.kurumi.ntt.utils.NTT;

public class UnMuteAll extends Fragment {

	@Override
	public void init(BotFragment origin) {
	
		super.init(origin);
		
		registerFunction("unmuteall");
		
	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
		requestTwitter(user,msg);
		
	}

	@Override
	public void onTwitterFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {
		
		try {
			
			Twitter api = account.createApi();

			long[] all = TApi.getAllMuteIDs(api);
			
			for (long id : all) {
				
				api.destroyMute(id);
				
			}
			
		} catch (TwitterException e) {
			
			msg.send(NTT.parseTwitterException(e)).exec();
			
		}
		
		msg.send("完成").exec();

	}
	
}

