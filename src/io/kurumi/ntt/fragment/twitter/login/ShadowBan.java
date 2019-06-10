package io.kurumi.ntt.fragment.twitter.login;
import cn.hutool.core.util.NumberUtil;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.fragment.abs.TwitterFunction;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.archive.UserArchive;
import io.kurumi.ntt.utils.NTT;
import java.util.LinkedList;
import twitter4j.Twitter;
import twitter4j.TwitterException;

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
			
			message.append("SearchBan : ").append(NTT.parseTwitterException(e)).append("\n");
			
		}
		
		status.edit(message.toString()).html().exec();
		
		try {

			boolean ban = NTT.testThreadBan(api,archive);

			message.append("ThreadBan : ").append(ban ? "有" : "无").append("\n");

		} catch (TwitterException e) {

			message.append("ThreadBan : ").append(NTT.parseTwitterException(e)).append("\n");

		}
		
		status.edit(message.toString()).html().exec();
		
		try {

			boolean ban = NTT.testSearchSuggestionBan(api,archive);

			message.append("SearchSuggestionBan : ").append(ban ? "有" : "无").append("\n");

		} catch (TwitterException e) {

			message.append("SearchSuggestionBan : ").append(NTT.parseTwitterException(e)).append("\n");

		}
		
		
		status.edit(message.toString()).html().exec();

	}

	@Override
	public void functions(LinkedList<String> names) {
		
		names.add("banstat");
	
	}

}
