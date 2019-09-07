package io.kurumi.ntt.fragment.twitter.ext;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.fragment.twitter.TAuth;
import twitter4j.TwitterException;
import twitter4j.Place;
import cn.hutool.json.JSONObject;
import io.kurumi.ntt.Launcher;
import io.kurumi.ntt.utils.NTT;
import io.kurumi.ntt.fragment.BotFragment;

public class GeoTest extends Fragment {

	@Override
	public void init(BotFragment origin) {
		// TODO: Implement this method
		super.init(origin);
		registerFunction("geo");
	}

	
	
	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
		requestTwitter(user,msg);
		
	}

	@Override
	public void onTwitterFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {
		
		try {
			
			Place location = account.createApi().getGeoDetails(params[0]);

			msg.send(new JSONObject(Launcher.GSON.toJson(location)).toStringPretty()).async();
			
		} catch (TwitterException e) {
			
			msg.send(NTT.parseTwitterException(e)).async();
			
		}

	}
	
}
