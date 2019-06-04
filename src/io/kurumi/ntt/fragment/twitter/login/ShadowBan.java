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
		
		Msg status = msg.send("正在测试...").send();
		
		Twitter api = account.createApi();

		UserArchive archive;
		
		if (NumberUtil.isNumber(params[0])) {
			
			try {
				
				archive = UserArchive.save(api.showUser(NumberUtil.parseLong(params[0])));
				
			} catch (TwitterException e) {
				
			status.edit(NTT.parseTwitterException(e)).exec();
				
				return;
				
			}

		} else {
			
			try {

				archive = UserArchive.save(api.showUser(NTT.parseScreenName(params[0])));

			} catch (TwitterException e) {

				status.edit(NTT.parseTwitterException(e)).exec();

				return;

			}
			
		}
	
		StringBuilder message = new StringBuilder("测试中... : ").append(archive.urlHtml()).append("\n");
		

		try {
			
			boolean searchBan = NTT.testSearchBan(api,archive);

			message.append("SearchBan : ").append(searchBan ? "有" : "无").append("\n");
			
		} catch (TwitterException e) {
			
			message.append("SearchBan : 测试失败\n");
			
		}
		
		try {

			boolean ban = NTT.testThreadBan(api,archive);

			message.append("ThreadBan : ").append(ban ? "有" : "无").append("\n");

		} catch (TwitterException e) {

			message.append("ThreadBan : 测试失败\n");

		}
		
		try {

			boolean ban = NTT.testSearchSuggestionBan(api,archive);

			message.append("SearchSuggestionBan : ").append(ban ? "有" : "无").append("\n");

		} catch (TwitterException e) {

			message.append("SearchSuggestionBan : 测试失败\n");

		}
		
		
		status.edit(message.toString()).exec();

	}

	@Override
	public void functions(LinkedList<String> names) {
		
		names.add("banstat");
	
	}

}
