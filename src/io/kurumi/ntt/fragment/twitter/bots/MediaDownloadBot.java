package io.kurumi.ntt.fragment.twitter.bots;

import io.kurumi.ntt.db.DataLongArray;
import io.kurumi.ntt.db.LongArrayData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.model.Msg;
import java.util.HashMap;
import twitter4j.FilterQuery;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.StatusListener;

public class MediaDownloadBot extends Fragment {

	public static LongArrayData data = new LongArrayData("TDBot");

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerAdminFunction("vdb_init","vdb_unset");

	}
	
	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
		requestTwitter(user,msg,true);
		
	}
	
	public static HashMap<Long,StatusListener> bots = new HashMap<>();
	
	public static StatusListener getListener(TAuth auth) {
		
		if (bots.containsKey(auth.id)) return bots.get(auth.id);
		
		StatusListener listener = new MDListener(auth);
		
		bots.put(auth.id,listener);
		
		return listener;
		
	}

	@Override
	public void onTwitterFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {
		
		if (function.endsWith("init")) {
			
			if (data.containsId(account.id)) {
				
				msg.send("已经注册过").async();
				
			} else {
				
				data.add(account.id);
			
				msg.send("已经启动").async();
				
			}
			
		} else {
			
			if (!data.containsId(account.id)) {

				msg.send("没有注册过").async();

			} else {
				
				data.deleteById(account.id);
								
				msg.send("已经停止").async();

			}
			
			
		}
		
	}
	

}
