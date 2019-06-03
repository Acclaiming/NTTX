package io.kurumi.ntt.fragment.twitter.status;

import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.model.request.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.twitter.archive.*;
import twitter4j.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.funcs.abs.*;
import java.util.*;
import io.kurumi.ntt.fragment.twitter.status.StatusAction.*;
import io.kurumi.ntt.utils.*;

public class StatusAction extends TwitterFunction {

	public static Data<CurrentAccount> current = new Data<CurrentAccount>(CurrentAccount.class);

	public static class CurrentAccount {

		public long id;

		public long accountId;

	}

	@Override
	public void functions(LinkedList<String> names) {

		names.add("current");

	}

	@Override
	public void onFunction(final UserData user,Msg msg,String function,String[] params,final TAuth account) {

		current.setById(user.id,new CurrentAccount() {{

					id = user.id;

					accountId = account.id;

				}});

		msg.send("当前操作账号已设为 : " + account.archive().urlHtml(),"当多用户时，可用此命令设置默认账号。").html().exec();

	}

	static final String POINT_RETWEET_STATUS = "s_rt";
	static final String POINT_DESTROY_RETWEET = "s_del";
	static final String POINT_DESTROY_STATUS = "s_del";
	static final String POINT_SHOW_FULL = "s_full";
	static final String POINT_LIKE_STATUS = "s_like";
	static final String POINT_UNLIKE_STATUS = "s_ul";

	@Override
	public void points(LinkedList<String> points) {
		
		points.add(POINT_LIKE_STATUS);
		points.add(POINT_UNLIKE_STATUS);
		points.add(POINT_RETWEET_STATUS);
		points.add(POINT_DESTROY_RETWEET);
		points.add(POINT_DESTROY_STATUS);
		points.add(POINT_SHOW_FULL);
		
	}
	
	public static ButtonMarkup createMarkup(final Status status,final boolean del,final boolean full) {

		return new ButtonMarkup() {{

				ButtonLine line = newButtonLine();

				if (status.isRetweetedByMe()) {

					line.newButton("❎️",POINT_DESTROY_RETWEET,status.getCurrentUserRetweetId(),full);

				} else {

					line.newButton("🔄",POINT_RETWEET_STATUS,status.getId(),full);

				}

				if (status.isFavorited()) {

					line.newButton("💔",POINT_UNLIKE_STATUS,status.getId(),full);

				} else {

					line.newButton("❤",POINT_LIKE_STATUS,status.getId(),full);

				}

				if (del) {

					line.newButton("❌️",POINT_DESTROY_STATUS,status.getId());

				}

				if (!full) {

					line.newButton("🔎",POINT_SHOW_FULL,status.getId());

				}

				// line.newButton("🔇",POINT_MUTE_USER,status.getUser().getId());


			}};

	}

	@Override
	public void onCallback(UserData user,Callback callback,String point,String[] params) {

		long statusId = Long.parseLong(params[0]);

		boolean isFull = params.length > 1 && "true".equals(params[1]);

		long count = TAuth.data.countByField("user",user.id);

		if (count == 0) {

			callback.alert("这需要认证Twitter账号才可使用 :) \n请私聊BOT使用 /login");

			return;
		}

		TAuth auth;

		if (count > 1) {

			CurrentAccount currentAccount = current.getById(user.id);

			if (currentAccount != null) {

				auth = TAuth.getById(currentAccount.accountId);

				if (auth == null || !auth.user.equals(user.id)) {

					callback.alert("乃认证了多个账号 请使用 /current 选择默认账号再操作 ~");

					return;

				}

			} else {

				callback.alert("乃认证了多个账号 请使用 /current 选择默认账号再操作 ~");

				return;

			}

		} else {

			auth = TAuth.getByUser(user.id).first();

		}

		Twitter api = auth.createApi();

		Status status;

		try {

			status = api.showStatus(statusId);

		} catch (TwitterException e) {

			callback.alert(NTT.parseTwitterException(e));

			return;

		}

		if (POINT_LIKE_STATUS.equals(point)) {

			try {

				api.createFavorite(statusId);

				callback.text("已打心 ~");

			} catch (TwitterException e) {

				callback.alert(NTT.parseTwitterException(e));

			}

		} else if (POINT_UNLIKE_STATUS.equals(point)) {

			try {

				api.destroyStatus(statusId);

				callback.text("已取消打心 ~");

			} catch (TwitterException e) {

				callback.alert(NTT.parseTwitterException(e));

			}

		} else if (POINT_RETWEET_STATUS.equals(point)) {

			try {

				api.retweetStatus(statusId);

				callback.text("已转推 ~");

			} catch (TwitterException e) {

				callback.alert(NTT.parseTwitterException(e));

			}

		} else if (POINT_DESTROY_STATUS.equals(point)) {

			try {

				api.destroyStatus(statusId);

				callback.text("已删除推文 ~");

				callback.delete();

				return;

			} catch (TwitterException e) {

				callback.alert(NTT.parseTwitterException(e));

			}

		} else if (POINT_DESTROY_RETWEET.equals(point)) {

			try {

				api.destroyStatus(statusId);

				callback.text("已撤销转推 ~");

			} catch (TwitterException e) {

				callback.alert(NTT.parseTwitterException(e));

			}
		
		} else if (POINT_SHOW_FULL.equals(point)) {

			callback.edit(StatusArchive.save(status,api).toHtml()).buttons(createMarkup(status,status.getUser().equals(auth.id),true)).html().exec();
			
			callback.text("已展开 ~");
			
			return;

		}

		callback.editMarkup(createMarkup(status,status.getUser().equals(auth.id),isFull));

	}


}
