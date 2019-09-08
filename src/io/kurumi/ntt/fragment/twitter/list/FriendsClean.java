package io.kurumi.ntt.fragment.twitter.list;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.fragment.twitter.TAuth;
import twitter4j.Twitter;
import io.kurumi.ntt.fragment.twitter.TApi;
import java.util.LinkedList;
import twitter4j.TwitterException;
import twitter4j.User;
import java.util.List;
import cn.hutool.core.collection.CollectionUtil;
import twitter4j.ResponseList;
import cn.hutool.core.util.ArrayUtil;
import io.kurumi.ntt.fragment.twitter.archive.UserArchive;
import io.kurumi.ntt.utils.NTT;
import java.util.Iterator;

public class FriendsClean extends Fragment {

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerFunction("clean_friends");

	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		if (params.length == 0 || !params[0].matches("[aopisl]*")) {

			String message = "清理正在关注 : /" + function + " <参数...>\n\n";

			message += "a - 清理所有\no - 单向关注\np - 没有锁推\ni - 没有头像\ns - 没有发过推文\nl - 没有打心";

			message += "\n\n注意 : 多个筛选参数叠加时都匹配才清理 ( 设定 a 时 其他参数不生效 )";

			msg.send(message).async();

			return;

		}

		requestTwitter(user,msg,true);

	}

	@Override
	public void onTwitterFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {

		Msg status = msg.send("正在查找...").send();

		String param = params[0];

		boolean a = param.contains("a");

		boolean o = param.contains("o");

		boolean p = param.contains("p");

		boolean i = param.contains("i");

		boolean s = param.contains("s");

		boolean l = param.contains("l");

		Twitter api = account.createApi();

		LinkedList<User> friends;

		try {

			LinkedList<Long> friendsIds = TApi.getAllFrIDs(api,account.id);

			if (o) {

				friendsIds.removeAll(TApi.getAllFoIDs(api,account.id));

			}

			friends = NTT.lookupUsers(api,friendsIds);

			Iterator<User> iter = friends.iterator();

			while (iter.hasNext()) {

				User target = iter.next();

				if (p && target.isProtected()) {

					iter.remove();

				} else if (i && !target.isDefaultProfileImage()) {

					iter.remove();

				} else if (s && target.getStatusesCount() > 0) {

					iter.remove();

				} else if (l && target.getFavouritesCount() > 0) {

					iter.remove();

				}

			}

		} catch (TwitterException e) {

			status.edit(NTT.parseTwitterException(e)).async();

			return;

		}

		if (friends.isEmpty()) {

			status.edit("没有目标用户 )").async();

			return;

		}

		status.edit("正在清理...").async();

		LinkedList<User> successful = new LinkedList<>();
		LinkedList<User> failed = new LinkedList<>();

		for (User target : friends) {

			try {

				api.destroyFriendship(target.getId());

				successful.add(target);

			} catch (TwitterException e) {

				failed.add(target);

			}

		}

		status.delete();

		StringBuilder message = new StringBuilder();

		if (!successful.isEmpty()) {

			message.append("已清理 : \n\n");

			for (User su : successful) {

				message.append(UserArchive.save(su).bName()).append("\n");

			}
			
			message.append("\n");

		}
		
		if (!failed.isEmpty()) {

			message.append("清理失败 : \n");

			for (User su : failed) {

				message.append("\n").append(UserArchive.save(su).bName());

			}

		}
		
		msg.send(message.toString()).html().async();

	}

}
