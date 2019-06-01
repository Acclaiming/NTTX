package io.kurumi.ntt.fragment.twitter.status;

import cn.hutool.core.util.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.funcs.abs.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.twitter.*;
import java.util.*;
import twitter4j.*;
import io.kurumi.ntt.utils.*;
import io.kurumi.ntt.twitter.archive.*;

public class StatusFetch extends TwitterFunction {

	@Override
	public void functions(LinkedList<String> names) {
		
		names.add("fetch");
		
	}
	
	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {
		
		if (params.length == 0) {
			
			msg.send("/fetch <用户ID|用户名|链接>").exec();
			
			return;
			
		}
		
		Twitter api = account.createApi();

		User target;
		
		if (NumberUtil.isNumber(params[0])) {
			
			try {
				
				target = api.showUser(NumberUtil.parseLong(params[0]));
				
			} catch (TwitterException e) {
				
				msg.send("找不到这个用户").exec();
				
				return;
				
			}

		} else {
			
			try {

				target = api.showUser(NTT.parseScreenName(params[0]));

			} catch (TwitterException e) {

				msg.send("找不到这个用户").exec();

				return;

			}
			
			
		}
		
		int count = 0;
		
		try {
			
			ResponseList<Status> tl = api.getUserTimeline(target.getScreenName(),new Paging(800));

			long sinceId = -1;
			
			for (Status s : tl) {
				
				if (s.getId() < sinceId || sinceId == -1) {
					
					sinceId = s.getId();
			
				}
				
				System.out.println(s);
				
				StatusArchive.save(s);
				
				count ++;
				
			}
			
			while (!tl.isEmpty()) {
				
				tl = api.getUserTimeline(target.getScreenName(),new Paging(800).maxId(sinceId));
				
				for (Status s : tl) {

					if (s.getId() < sinceId || sinceId == -1) {

						sinceId = s.getId();

					}

					StatusArchive.save(s);
					
					System.out.println(s);

					count ++;

				}
				
			}
			
			msg.send(count + "条推文已拉取").exec();

		} catch (TwitterException e) {
			
			msg.send("拉取失败 : " + e).exec();
			
		}

	}
	
}
