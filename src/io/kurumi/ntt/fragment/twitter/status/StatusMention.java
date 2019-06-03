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
import io.kurumi.ntt.fragment.twitter.status.StatusMention.*;

public class StatusMention extends TwitterFunction {

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

	static final String POINT_RETWEET_STATUS = "s_ret";
	static final String POINT_DESTROY_STATUS = "s_del";

	static final String POINT_LIKE_STATUS= "s_like";
	static final String POINT_UNLIKE_STATUS = "s_unlike";

	public static ButtonMarkup createMarkup(final Status status,final boolean del) {

		return new ButtonMarkup() {{

				ButtonLine line = newButtonLine();

				if (status.isRetweetedByMe()) {

					line.newButton("❎️",POINT_DESTROY_STATUS,status.getCurrentUserRetweetId());

				} else {

					line.newButton("🔄",POINT_RETWEET_STATUS,status.getId());

				}

				if (status.isFavorited()) {

					line.newButton("💔",POINT_UNLIKE_STATUS,status.getId());

				} else {

					line.newButton("❤",POINT_LIKE_STATUS,status.getId());

				}

				if (del) {

					line.newButton("❌️",POINT_DESTROY_STATUS,status.getId());

				}

				// line.newButton("🔇",POINT_MUTE_USER,status.getUser().getId());


			}};

	}

	@Override
	public void onCallback(UserData user,Callback callback,String point,String[] params) {

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
	
		long statusId = Long.parseLong(params[0]);
		
		if (POINT_LIKE_STATUS.equals(point)) {
			
			try {
				
				api.createFavorite(statusId);
				
			} catch (TwitterException e) {}

		}

	}


}
