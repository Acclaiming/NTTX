package io.kurumi.ntt.fragment.debug;
import cn.hutool.core.util.NumberUtil;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.fragment.abs.TwitterFunction;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.utils.NTT;
import java.util.LinkedList;
import twitter4j.TwitterException;

public class DebugUser extends TwitterFunction {

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {
		
		if (params.length == 0) {
			
			msg.send("invaild user").exec();
			
			return;
			
		}
		
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

	@Override
	public void functions(LinkedList<String> names) {
		
		names.add("get_user");
		
	}

}
