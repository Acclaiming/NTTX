package io.kurumi.ntt.fragment.twitter.login;
import io.kurumi.ntt.funcs.abs.*;
import io.kurumi.ntt.model.*;
import java.util.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.db.*;
import cn.hutool.core.util.*;
import io.kurumi.ntt.twitter.archive.*;
import twitter4j.*;
import io.kurumi.ntt.utils.*;

public class ShadowBan extends TwitterFunction {

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {
		
		if (params.length == 0) {
			
			msg.send("/banstat <ID|用户名|链接>").exec();
			
			return;
			
		}
		
		Twitter api = account.createApi();

		UserArchive archive;
		
		if (NumberUtil.isNumber(params[0])) {
			
			try {
				
				archive = UserArchive.save(api.showUser(NumberUtil.parseLong(params[0])));
				
			} catch (TwitterException e) {
				
				msg.send(NTT.parseTwitterException(e)).exec();
				
				return;
				
			}

		}
		
	}

	@Override
	public void functions(LinkedList<String> names) {
		
		names.add("banstat");
	
	}

}
