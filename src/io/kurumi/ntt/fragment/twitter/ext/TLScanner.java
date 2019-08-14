package io.kurumi.ntt.fragment.twitter.ext;

import cn.hutool.core.util.ArrayUtil;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.archive.UserArchive;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.Html;
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
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;

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

		String blockedBy = "";
		String locked = "";

		for (int index = 0;iter.hasNext();index ++) {

			Long userId = iter.next();

			target.add(userId);

			try {

				ResponseList<Status> tl = api.getUserTimeline(userId,new Paging().count(20));

				for (Status status : tl) {

					if (status.isRetweet()) {

						target.add(status.getRetweetedStatus().getUser().getId());

					} else for (UserMentionEntity m : status.getUserMentionEntities()) {

							target.add(m.getId());

						}

				}

			} catch (TwitterException e) {

				if (e.getErrorCode() == 136) {

					UserArchive bb = UserArchive.show(api,userId);

					blockedBy += "\n" + Html.b(bb.name) + " " + Html.a("@" + bb.screenName,bb.url());

				} else if (e.getStatusCode() == 401 && e.getErrorCode() == -1) {

					UserArchive bb = UserArchive.show(api,userId);

					locked += "\n" + Html.b(bb.name) + " " + Html.a("@" + bb.screenName,bb.url());

				} else {

					stat.edit(NTT.parseTwitterException(e)).async();

					return;

				}

			}

			stat.edit("正在扫描 已发现 " + target.size() + " 项 ( " + (index + 1) + " / " + ids.size() + " )").async();

		}

		stat.edit("扫描完成 已发现 " + target.size() + " 项 正在解析...").async();

		float value = 0;

		if (!StrUtil.isEmpty(blockedBy)) {

			value -= blockedBy.split("\n").length * 4;

			blockedBy = Html.b("\n你被他们屏蔽了，所以这个结果可能不准确 :\n") + blockedBy;

		} else {

			blockedBy = Html.b("\n没有被你圈子里的任何人屏蔽。");

		}

		if (!StrUtil.isEmpty(locked)) {

			value -= locked.split("\n").length * 2;

			locked = Html.b("\n这些用户锁推了，所以这个结果可能不准确 :\n") + locked;

		} else {

			locked = Html.b("圈子里没有未关注的锁推用户。");

		}


		float max = target.size();

		int fr = 0;
		int fo = 0;
		int tw = 0;

		while (!target.isEmpty()) {

			// 被屏蔽 -3 互相关注 +2 单向关注 -1 单向被关注 +1 无 0 / size * 2

			iter = target.iterator();

			LinkedList<Long> current = new LinkedList<>();

			for (int index = 0;index < 100;index ++) {

				if (iter.hasNext()) current.add(iter.next());

				else break;

			}

			target.removeAll(current);

			try {

				ResponseList<Friendship> ships = api.lookupFriendships(ArrayUtil.unWrap(current.toArray(new Long[current.size()])));

				for (Friendship ship : ships) {

					if (ship.isFollowedBy() && ship.isFollowing()) {

						value += 2;

						tw ++;

					} else if (ship.isFollowedBy()) {

						value ++;

						fo ++;

					} else if (ship.isFollowing()) {

						value --;

						fr ++;

					}

				}

			} catch (TwitterException e) {

				stat.edit("接口调用频繁 (请过 15分钟 再试)").exec();

				return;

			}

			if (!target.isEmpty()) {

				stat.edit("正在解析... " + ((int)(max - target.size())) + " / " + ((int)max)).exec();

			}

		}

		String status = "圈子共有 " + ids.size() + " 人 外扩至 " + ((int)max) + " 人 :";

		status += "\n\n与 " + Html.b(tw) + " 人互相关注";
		status += "\n单向关注 " + Html.b(fr) + " 人";
		status += "\n被 " + Html.b(fo) + " 人单向关注";

		double result = ((double)(((int)((value / (max)) * 10000)) / 100));
		
		stat.edit(Html.b("你的结果是 : " + result + "%"),HtmlUtil.escape(status),locked,blockedBy).html().async();

	}

}
