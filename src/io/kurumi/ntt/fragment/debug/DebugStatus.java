package io.kurumi.ntt.fragment.debug;

import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.fragment.abs.TwitterFunction;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.utils.NTT;
import java.util.LinkedList;
import twitter4j.Status;
import twitter4j.TwitterException;

public class DebugStatus extends TwitterFunction {

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {
		
		if (params.length == 0) {
			
			msg.send("invailed status id").exec();
			
			return;
			
		}
		
		try {
			
			Status status = account.createApi().showStatus(NTT.parseStatusId(params[0]));

			msg.send(status.toString()).exec();
			
		} catch (TwitterException e) {
			
			msg.send(NTT.parseTwitterException(e)).exec();
			
		}

	}

	@Override
	public void functions(LinkedList<String> names) {
		
		names.add("get_status");
		
	}

}
