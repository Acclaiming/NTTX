package io.kurumi.ntt.fragment.twitter.action;

import cn.hutool.core.util.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.funcs.abs.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.utils.*;
import java.util.*;
import twitter4j.*;
import io.kurumi.ntt.twitter.archive.*;
import com.pengrad.telegrambot.request.*;
import io.kurumi.ntt.fragment.twitter.status.*;
import io.kurumi.ntt.funcs.twitter.track.*;
import io.kurumi.ntt.funcs.twitter.track.TrackTask.*;

public class Block extends TwitterFunction {

	@Override
	public void functions(LinkedList<String> names) {

		names.add("block");

	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {

		Twitter api = account.createApi();

		if (params.length > 0) {

			for (String target : params) {

				long targetId;

				if (NumberUtil.isNumber(target)) {

					targetId = NumberUtil.parseLong(target);

				} else {

					String screenName = NTT.parseScreenName(target);

					try {

						targetId =  api.showUser(screenName).getId();

					} catch (TwitterException e) {

						msg.send(NTT.parseTwitterException(e)).exec();

						return;

					}

				}

				block(user,msg,account,api,targetId);

			}

		} else if (msg.targetChatId == -1 && msg.isPrivate() && msg.isReply()) {

			MessagePoint point = MessagePoint.get(msg.replyTo().messageId());

			if (point == null) {

				msg.send("咱不知道目标是谁 (｡í _ ì｡)").exec();

				return;

			}

			long targetUser;

			if (point.type == 1) {

				targetUser = StatusArchive.get(point.targetId).from;

			} else {

				targetUser = point.targetId;

			}

			block(user,msg,account,api,targetUser);

		} else {

			msg.send("/block <ID|用户名|链接> / 或对私聊消息回复 (如果你觉得这条消息包含一个用户或推文)").exec();

			return;

		}

	}

	private void block(UserData user,Msg msg,TAuth account,Twitter api,long targetId) {

		UserArchive archive;

		try {

			archive = UserArchive.save(api.showUser(targetId));

		} catch (TwitterException e) {

			msg.send(NTT.parseTwitterException(e)).exec();

			return;

		}

		try {

			Relationship ship = api.showFriendship(account.id,targetId);

			if (ship.isSourceBlockingTarget()) {

				msg.send("你已经把 " + archive.urlHtml() + " 屏蔽了 ~").html().point(0,targetId);

				return;

			}

			api.createBlock(targetId);

			TrackTask.IdsList fo = TrackTask.followers.getById(account.id);

			fo.ids.remove(targetId);
			
			TrackTask.followers.setById(account.id,fo);
			
			msg.send("已屏蔽 " + archive.urlHtml() + " ~").html().point(0,targetId);

		} catch (TwitterException e) {

			msg.send("屏蔽失败 :",NTT.parseTwitterException(e)).exec();

		}

	}

}

