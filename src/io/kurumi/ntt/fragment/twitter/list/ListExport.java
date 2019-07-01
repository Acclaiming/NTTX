package io.kurumi.ntt.fragment.twitter.list;

import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.fragment.abs.request.Keyboard;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.BotFragment;
import twitter4j.Twitter;
import io.kurumi.ntt.fragment.twitter.TApi;
import twitter4j.TwitterException;
import java.util.LinkedList;
import com.pengrad.telegrambot.request.SendDocument;
import cn.hutool.core.util.ArrayUtil;
import io.kurumi.ntt.utils.NTT;

public class ListExport extends Fragment {

	final String FOLLOWING = "关注中列表";
	final String FOLLOWER = "关注者列表";
	final String BLOCK = "屏蔽列表";
	final String MUTE = "静音列表";
	final String USER = "其他列表";
	
	final String POINT_LIST_EXPORT = "list_export";

	@Override
	public void init(BotFragment origin) {

		super.init(origin);
		
		registerFunction("export");
		
		registerPoints(POINT_LIST_EXPORT);
		
	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
		requestTwitter(user,msg);
		
	}

	@Override
	public void onTwitterFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {
		
		setPrivatePoint(user,POINT_LIST_EXPORT,account);
		
		msg
			.send("请选择将要导出的列表 :","将会以 .csv 官方格式导出")
			.keyboard(new Keyboard() {{

					newButtonLine().newButton(FOLLOWING).newButton(FOLLOWER);
					newButtonLine().newButton(BLOCK).newButton(MUTE);

					// newButtonLine(USER);

				}})
			.withCancel()
			.exec();
		
	}

	@Override
	public void onPoint(UserData user,Msg msg,String point,Object data) {
		
		TAuth account = (TAuth) data;
		
		Twitter api = account.createApi();

		if (FOLLOWING.equals(msg.text())) {
			
			try {
				
				LinkedList<Long> ids = TApi.getAllFrIDs(api,account.id);

				bot().execute(new SendDocument(msg.chatId(),ArrayUtil.join(ids.toArray(),"\n")).fileName("FollowingList.csv"));
				
			} catch (TwitterException e) {
				
				msg.send("导出失败",NTT.parseTwitterException(e)).exec();
				
			}

		} else if (FOLLOWER.equals(msg.text())) {
			
			try {

				LinkedList<Long> ids = TApi.getAllFoIDs(api,account.id);

				bot().execute(new SendDocument(msg.chatId(),ArrayUtil.join(ids.toArray(),"\n")).fileName("FollowingList.csv"));

			} catch (TwitterException e) {

				msg.send("导出失败",NTT.parseTwitterException(e)).exec();

			}
			
			
		} else if (BLOCK.equals(msg.text())) {
			
			try {

				long[] ids = TApi.getAllBlockIDs(api);

				bot().execute(new SendDocument(msg.chatId(),ArrayUtil.join(ids,"\n")).fileName("FollowingList.csv"));

			} catch (TwitterException e) {

				msg.send("导出失败",NTT.parseTwitterException(e)).exec();

			}
			
			
		} else if (MUTE.equals(msg.text())) {
			
			try {

				long[] ids = TApi.getAllMuteIDs(api);

				bot().execute(new SendDocument(msg.chatId(),ArrayUtil.join(ids,"\n")).fileName("FollowingList.csv"));

			} catch (TwitterException e) {

				msg.send("导出失败",NTT.parseTwitterException(e)).exec();

			}
			
		} else {
			
			msg.send("要导出什么？").exec();
			
			return;
			
		}
		
		clearPrivatePoint(user);
		
	}
	
}
