package io.kurumi.ntt.fragment.twitter.bot;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.db.LongArrayData;
import io.kurumi.ntt.fragment.twitter.TAuth;
import twitter4j.TwitterStreamFactory;
import twitter4j.FilterQuery;
import twitter4j.TwitterStream;
import java.util.HashMap;
import io.kurumi.ntt.db.DataLongArray;
import twitter4j.TwitterStreamImpl;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;

public class VideoDownloadBot extends Fragment {

	public static LongArrayData data = new LongArrayData("TDBot");

	public static HashMap<Long,TwitterStream> bots = new HashMap<>();

	public static void startAll() {
		
		for (DataLongArray accountId : data.getAll()) {
			
			startBot(accountId.id);
			
		}
		
	}
	
	public static void stopAll() {
		
		for (TwitterStream stream : bots.values()) stream.shutdown();
		
	}
	
	public static void startBot(long accountId) {

		TAuth account = TAuth.getById(accountId);

		TwitterStream stream = new TwitterStreamFactory(account.createConfig()).getInstance()

			.addListener(new VideoDownloadListener(account))

			.filter(new FilterQuery()
			
					.follow(new long[] {account.id}));

		bots.put(account.id,stream);
					
	}

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerAdminFunction("vdb_init","vdb_unset");

	}
	
	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
		requestTwitter(user,msg,true);
		
	}

	@Override
	public void onTwitterFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {
		
		if (function.endsWith("init")) {
			
			if (data.containsId(account.id)) {
				
				msg.send("已经注册过").async();
				
			} else {
				
				data.add(account.id);
				
				startBot(account.id);
				
				msg.send("已经启动").async();
				
			}
			
		} else {
			
			if (!data.containsId(account.id)) {

				msg.send("没有注册过").async();

			} else {
				
				data.deleteById(account.id);
				
				TwitterStream stream = bots.remove(account.id);
				
				if (stream != null) stream.shutdown();
				
				msg.send("已经停止").async();

			}
			
			
		}
		
	}
	

}
