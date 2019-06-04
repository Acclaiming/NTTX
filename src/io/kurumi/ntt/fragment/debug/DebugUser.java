package io.kurumi.ntt.fragment.debug;
import io.kurumi.ntt.funcs.abs.*;
import io.kurumi.ntt.model.*;
import java.util.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.db.*;
import cn.hutool.core.util.*;
import twitter4j.*;
import io.kurumi.ntt.utils.*;

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
