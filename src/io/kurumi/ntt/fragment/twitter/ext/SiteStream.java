package io.kurumi.ntt.fragment.twitter.ext;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.fragment.twitter.TAuth;
import twitter4j.TwitterStreamFactory;
import twitter4j.TwitterStream;
import twitter4j.StatusListener;
import twitter4j.StatusDeletionNotice;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.FilterQuery;
import io.kurumi.ntt.fragment.twitter.archive.StatusArchive;

public class SiteStream extends Fragment {

	@Override
	public void init(BotFragment origin) {
		
		super.init(origin);
		
		registerFunction("ss");
		
	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
		requestTwitter(user,msg);
		
	}

	@Override
	public void onTwitterFunction(UserData user,final Msg msg,String function,String[] params,final TAuth account) {
		
		TwitterStream stream = new TwitterStreamFactory(account.createConfig()).getInstance();

		stream.addListener(new StatusListener() {

				@Override
				public void onStatus(Status status) {
				
					StatusArchive.save(status).sendTo(msg.chatId(),0,account,status);
					
				}

				@Override
				public void onDeletionNotice(StatusDeletionNotice p1) {
				}

				@Override
				public void onTrackLimitationNotice(int p1) {
				}

				@Override
				public void onScrubGeo(long p1,long p2) {
				}

				@Override
				public void onStallWarning(StallWarning p1) {
				}

				@Override
				public void onException(Exception p1) {
					// TODO: Implement this method
				}
			});
		
		stream.sample(msg.param());
		
	}
	
}
