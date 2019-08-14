package io.kurumi.ntt.fragment.twitter.ext;

import cn.hutool.core.util.ArrayUtil;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.NTT;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import twitter4j.Friendship;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.UserMentionEntity;

public class TLScanner extends Fragment {

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerFunction("tl_scan");

	}


	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		requestTwitter(user,msg,true);

	}

	@Override
	public void onTwitterFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {

		Twitter api = account.createApi();

		Msg stat = msg.send("正在读取时间线").send();

		HashSet<Long> ids = new HashSet<>();

		try {

			ResponseList<Status> tl = api.getHomeTimeline(new Paging().count(200));

			for (Status status : tl) {
				
				ids.add(status.getUser().getId());

				if (status.isRetweet()) ids.add(status.getRetweetedStatus().getUser().getId());
			
				else for (UserMentionEntity m : status.getUserMentionEntities()) ids.add(m.getId());

			}

		} catch (TwitterException e) {

			stat.edit(NTT.parseTwitterException(e)).async();

			return;

		}
		
		if (ids.isEmpty()) {
			
			stat.edit("时间线上什么也没有....").async();
			
			return;
			
		}
		
		HashSet<Long> target = new HashSet<>();
		
		Iterator<Long> iter = ids.iterator();

		for (int index = 0;iter.hasNext();index ++) {
			
			Long userId = iter.next();
			
			target.add(userId);
			
			try {

				ResponseList<Status> tl = api.getUserTimeline(userId,new Paging().count(200));

				for (Status status : tl) {

					if (status.isRetweet()) target.add(status.getRetweetedStatus().getUser().getId());
					
					else for (UserMentionEntity m : status.getUserMentionEntities()) target.add(m.getId());

				}

			} catch (TwitterException e) {

				stat.edit(NTT.parseTwitterException(e)).async();

				return;

			}
			
			stat.edit("正在扫描 已发现 " + target.size() + " 项 ( " + (index + 1) + " / " + ids.size() + " )").async();
			
		}

		stat.edit("扫描完成 已发现 " + target.size() + " 项 正在解析...").async();
		
		float value = 0;
		float max = target.size();
		
		while (!target.isEmpty()) {
			
			// 互相关注 +2 单向关注 -1 单向被关注 +1 无 0 / size * 2
			
			iter = target.iterator();
			
			LinkedList<Long> current = new LinkedList<>();
			
			for (int index = 0;index < 200;index ++) {
				
				if (iter.hasNext()) current.add(iter.next());
				
				else break;
				
			}
			
			try {
				
				ResponseList<Friendship> ships = api.lookupFriendships(ArrayUtil.unWrap(current.toArray(new Long[current.size()])));

				for (Friendship ship : ships) {
					
					if (ship.isFollowedBy() && ship.isFollowing()) value += 2;
					else if (ship.isFollowedBy()) value ++;
					else if (ship.isFollowing()) value --;
					
				}
				
			} catch (TwitterException e) {}
			
			stat.edit("正在解析... " + (max - target.size()) + " / " + max).async();

		}
		
		stat.edit("解析完成 你的结果是 : " + ((value / (max * 2)) * 100) + "% 你觉得怎么样呢？").async();
		
	}

}
