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
import io.kurumi.ntt.fragment.twitter.auto.*;
import io.kurumi.ntt.funcs.twitter.track.*;
import com.mongodb.client.*;
import io.kurumi.ntt.funcs.twitter.track.TrackTask.*;

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

		try {

			Relationship ship = api.showFriendship(target.getId(),account.id);

			if (target.isProtected() && !ship.isSourceFollowedByTarget()) {

				TrackTask.IdsList newAcc = TrackTask.friends.getByField("ids",target.getId());

				if (newAcc == null) {

					msg.send("这个人锁推了...").exec();

					return;

				}

				api = TAuth.getById(newAcc.id).createApi();

			} else if (ship.isSourceBlockingTarget()) {

				TrackTask.IdsList newAcc = TrackTask.friends.getByField("ids",target.getId());

				if (newAcc == null) {

					msg.send("这个人屏蔽了你...").exec();

					return;

				}

				api = TAuth.getById(newAcc.id).createApi();


			}

		} catch (TwitterException e) {

		}

		Msg status = msg.send("正在拉取...").send();

		int count = 0;

		int exists = 0;

		boolean all = params.length > 1 && params[1].equals("--all");

		try {

			ResponseList<Status> tl = api.getUserTimeline(target.getId(),new Paging().count(200));

			if (tl.isEmpty()) {

				status.edit("这个用户没有发过推文...").exec();

				return;

			}

			long sinceId = -1;

			for (Status s : tl) {

				if (s.getId() < sinceId || sinceId == -1) {

					sinceId = s.getId();

				}

				if (!all) {

					if (exists >= 20) {

						break;

					}

					if (StatusArchive.data.containsId(s.getId())) {

						exists ++;

						count --;

					} else {

						exists = 0;

					}

				}

				StatusArchive.save(s).loop(api);

				count ++;

			}

			status.edit("正在拉取中... : ",count + "条推文已拉取").exec();

			w:while (!tl.isEmpty()) {

				tl = api.getUserTimeline(target.getId(),new Paging().count(200).maxId(sinceId - 1));

				if (exists >= 10) {

					break w;

				}
				
				for (Status s : tl) {

					if (s.getId() < sinceId || sinceId == -1) {

						sinceId = s.getId();

					}

					if (!all) {

						if (exists >= 10) {
							
							break w;

						}

						if (StatusArchive.data.containsId(s.getId())) {
							
							exists ++;
							
							count --;
							
						} else {
							
							exists = 0;
							
						}
						
					}
					

					StatusArchive.save(s).loop(api);

					count ++;

				}


				if (tl.isEmpty()) break;

				status.edit("正在拉取中...",count + "条推文已拉取").exec();

			}

			status.edit("已拉取完成 :",count + "条推文已拉取").exec();

		} catch (TwitterException e) {

			msg.send("拉取失败 : " + e).exec();

		}

	}

}
