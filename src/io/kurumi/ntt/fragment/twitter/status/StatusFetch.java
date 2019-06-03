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

		String target = null;
		long targetL = -1;

		UserArchive archive = null;

		if (NumberUtil.isNumber(params[0])) {

			targetL = NumberUtil.parseLong(params[0]);
		
		} else {

			target = NTT.parseScreenName(params[0]);

		}
		
		Msg status = msg.send("正在拉取...").send();

		if (UserArchive.contains(targetL)) {

			archive = UserArchive.get(targetL);

		} else if (UserArchive.contains(target)) {

			archive = UserArchive.get(target);

		}

		boolean accessable = false;

		TwitterException exc = null;
		
		try {

			archive = UserArchive.save(targetL == -1 ? api.showUser(target) : api.showUser(targetL));

			try {

				Relationship ship = api.showFriendship(archive.id,account.id);

				if (!ship.isSourceBlockingTarget() && !(archive.isProtected && !ship.isSourceFollowedByTarget())) {

					accessable = true;

				}


			} catch (TwitterException e) {
				
				exc = e;

			}

		} catch (TwitterException ex) {

			if (ex.getErrorCode() != 136) {

				status.edit(NTT.parseTwitterException(ex)).exec();

				return;

			}

			exc = ex;
			
		}

		if (!accessable) {
			
			TAuth accessableAuth = NTT.loopFindAccessable(targetL == -1 ? target : targetL);
			
			if (accessableAuth == null) {
				
				if (exc != null) {
					
					status.edit(NTT.parseTwitterException(exc)).exec();
					
					return;
					
				} else {
					
					status.edit("这个人锁推了...").exec();
					
					return;
					
				}
				
			}
			
			api = accessableAuth.createApi();
			
			if (archive == null) {
				
				archive = targetL == -1 ? UserArchive.get(target) : UserArchive.get(targetL);
				
			}

		}
		
		int count = 0;

		int exists = 0;

		boolean all = params.length > 1 && params[1].equals("--all");

		try {

			ResponseList<Status> tl = api.getUserTimeline(archive.id,new Paging().count(200));

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

				tl = api.getUserTimeline(archive.id,new Paging().count(200).maxId(sinceId - 1));

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

			msg.send("拉取失败 : ",NTT.parseTwitterException(e)).exec();

		}

	}

}
