package io.kurumi.ntt.fragment.twitter.bot;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.db.LongArrayData;
import io.kurumi.ntt.fragment.twitter.TAuth;
import twitter4j.TwitterStreamFactory;
import twitter4j.FilterQuery;
import twitter4j.TwitterStream;
import java.util.HashMap;

public class VideoDownloadBot extends Fragment {

	public static LongArrayData data = new LongArrayData("TDBot");

	public static HashMap<Long,TwitterStream> bots = new HashMap<>();
	
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

}
