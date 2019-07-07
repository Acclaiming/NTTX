package io.kurumi.ntt.fragment.twitter.list;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.SendDocument;
import io.kurumi.ntt.db.PointData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.TApi;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.Keyboard;
import io.kurumi.ntt.utils.NTT;
import java.util.LinkedList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.UserList;

public class ListExport extends Fragment {

	final String FOLLOWING = "关注中列表";
	final String FOLLOWER = "关注者列表";
	final String BLOCK = "屏蔽列表";
	final String MUTE = "静音列表";
	final String USER = "用户创建的列表";

	final String POINT_LIST_EXPORT = "list_export";
	final String POINT_USER_LIST_EXPORT = "user_list_export";

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

		setPrivatePointData(user,POINT_LIST_EXPORT,account);

		msg
			.send("请选择将要导出的列表 :","将会以 .csv 官方格式导出")
			.keyboard(new Keyboard() {{

					newButtonLine().newButton(FOLLOWING).newButton(FOLLOWER);
					newButtonLine().newButton(BLOCK).newButton(MUTE);

					newButtonLine(USER);

				}})
			.withCancel()
			.exec();

	}

	@Override
	public void onPoint(UserData user,Msg msg,String point,PointData data) {

		TAuth account = data.data();

		Twitter api = account.createApi();

		if (POINT_LIST_EXPORT.equals(point)) {

			if (FOLLOWING.equals(msg.text())) {

				clearPrivatePoint(user);

				try {

					LinkedList<Long> ids = TApi.getAllFrIDs(api,account.id);

					if (ids.size() == 0) {

						msg.send("列表为空 :)").exec();

						return;

					}
					
					msg.sendUpdatingFile();

					bot().execute(new SendDocument(msg.chatId(),StrUtil.utf8Bytes(ArrayUtil.join(ids.toArray(),"\n"))).replyMarkup(new ReplyKeyboardRemove()).fileName("FollowingList.csv"));

				} catch (TwitterException e) {

					msg.send("导出失败",NTT.parseTwitterException(e)).exec();

				}

			} else if (FOLLOWER.equals(msg.text())) {

				clearPrivatePoint(user);

				try {

					LinkedList<Long> ids = TApi.getAllFoIDs(api,account.id);

					if (ids.size() == 0) {

						msg.send("列表为空 :)").exec();

						return;

					}
					
					msg.sendUpdatingFile();

					bot().execute(new SendDocument(msg.chatId(),StrUtil.utf8Bytes(ArrayUtil.join(ids.toArray(),"\n"))).replyMarkup(new ReplyKeyboardRemove()).fileName("FollowersList.csv"));

				} catch (TwitterException e) {

					msg.send("导出失败",NTT.parseTwitterException(e)).exec();

				}


			} else if (BLOCK.equals(msg.text())) {

				clearPrivatePoint(user);

				try {

					long[] ids = TApi.getAllBlockIDs(api);

					if (ids.length == 0) {
						
						msg.send("列表为空 :)").exec();
						
						return;
						
					}
					
					msg.sendUpdatingFile();

					bot().execute(new SendDocument(msg.chatId(),StrUtil.utf8Bytes(ArrayUtil.join(ids,"\n"))).replyMarkup(new ReplyKeyboardRemove()).fileName("BlockList.csv"));

				} catch (TwitterException e) {

					msg.send("导出失败",NTT.parseTwitterException(e)).exec();

				}


			} else if (MUTE.equals(msg.text())) {

				clearPrivatePoint(user);

				try {

					long[] ids = TApi.getAllMuteIDs(api);

					if (ids.length == 0) {

						msg.send("列表为空 :)").exec();

						return;

					}
					
					msg.sendUpdatingFile();

					bot().execute(new SendDocument(msg.chatId(),StrUtil.utf8Bytes(ArrayUtil.join(ids,"\n"))).replyMarkup(new ReplyKeyboardRemove()).fileName("MuteList.csv"));

				} catch (TwitterException e) {

					msg.send("导出失败",NTT.parseTwitterException(e)).exec();

				}

			} else if (USER.equals(msg.text())) {

				setPrivatePointData(user,POINT_USER_LIST_EXPORT,data);

				msg.send("现在请发送列表的链接 可以在列表 -> 分享 中导出。它看起来像这样 : twitter.com/用户名/lists/列表名").withCancel().removeKeyboard().exec();

			} else {

				msg.send("要导出什么？").withCancel().exec();

			}

		} else if (POINT_USER_LIST_EXPORT.equals(point)) {
			
			if (!(msg.text().contains("twitter.com/") && msg.text().contains("/lists/"))) {

				msg.send("要导出哪个 用户创建的列表？请发送该列表的链接 : 它看起来像 twitter.com/用户名/lists/列表名 ，可以在列表 -> 分享 导出。").exec();

				return;

			}
			
			clearPrivatePoint(user);

			String screenName = StrUtil.subBefore(StrUtil.subAfter(msg.text(),"lists/",true),"/",false);
			String slug = StrUtil.subAfter(msg.text(),screenName,true);
			
			if (slug.contains("?")) {
				
				slug = StrUtil.subBefore(slug,"?",false);
				
			}
			
			if (slug.contains(" ")) {
				
				slug = StrUtil.subBefore(slug," ",false);
				
			}
			
			UserList list;
			
			try {
				
				list = api.showUserList(screenName,slug);
				
			} catch (TwitterException e) {
				
				msg.send("查找列表失败",NTT.parseTwitterException(e)).exec();
				
				return;
				
			}
			
			
			long[] ids;
		
			try {
				
				LinkedList<User> userList = TApi.getListUsers(api,list.getId());

				ids = new long[userList.size()];
				
				if (ids.length == 0) {

					msg.send("列表为空 :)").exec();

					return;

				}
				
				for (int index = 0;index < ids.length;index ++) {
					
					ids[index] = userList.get(index).getId();
					
				}
				
			} catch (TwitterException e) {
				
				msg.send("导出列表失败",NTT.parseTwitterException(e)).exec();
				
				return;
				
			}
			
			msg.sendUpdatingFile();

			bot().execute(new SendDocument(msg.chatId(),StrUtil.utf8Bytes(ArrayUtil.join(ids,"\n"))).replyMarkup(new ReplyKeyboardRemove()).fileName(list.getSlug() + ".csv"));

		}

	}

}
