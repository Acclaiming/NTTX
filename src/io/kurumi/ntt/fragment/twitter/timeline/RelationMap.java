package io.kurumi.ntt.fragment.twitter.timeline;

import io.kurumi.ntt.funcs.abs.TwitterFunction;
import io.kurumi.ntt.twitter.TAuth;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.db.UserData;
import java.util.LinkedList;
import twitter4j.Twitter;
import twitter4j.Paging;
import twitter4j.TwitterException;
import twitter4j.ResponseList;
import twitter4j.Status;
import java.util.HashMap;

public class RelationMap extends TwitterFunction {

	public HashMap<Long,
	
	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {
	
		Twitter api = account.createApi();
		
		try {
			
			ResponseList<Status> timeline = api.getUserTimeline(new Paging().count(200));

			
			
		} catch (TwitterException e) {}

	}

	@Override
	public void functions(LinkedList<String> names) {
		// TODO: Implement this method
	}
	
	
	
	
}
