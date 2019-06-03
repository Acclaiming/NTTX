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

public class UnMute extends TwitterFunction {

	@Override
	public void functions(LinkedList<String> names) {

		names.add("unmute");

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

				unmute(user,msg,account,api,targetId);

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

			unmute(user,msg,account,api,targetUser);

		} else {

			msg.send("/block <ID|用户名|链接> / 或对私聊消息回复 (如果你觉得这条消息包含一个用户或推文)").exec();

			return;

		}

	}

	private void unmute(UserData user,Msg msg,TAuth account,Twitter api,long targetId) {

		UserArchive archive;

		try {

			archive = UserArchive.save(api.showUser(targetId));

		} catch (TwitterException e) {

			msg.send(NTT.parseTwitterException(e)).exec();

			return;

		}

		try {

			Relationship ship = api.showFriendship(account.id,targetId);

			if (!ship.isSourceMutingTarget()) {

				msg.send("你没有停用对 " + archive.urlHtml() + " 的通知 ~").html().point(0,targetId);

				return;

			}

			api.destroyMute(targetId);

			msg.send("已启用对 " + archive.urlHtml() + " 的通知 ~").html().point(0,targetId);

		} catch (TwitterException e) {

			msg.send("启用失败 :",NTT.parseTwitterException(e)).exec();

		}

	}

}

