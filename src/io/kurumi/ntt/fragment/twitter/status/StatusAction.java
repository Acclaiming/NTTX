package io.kurumi.ntt.fragment.twitter.status;

import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageCaption;
import com.pengrad.telegrambot.response.BaseResponse;
import io.kurumi.ntt.db.Data;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.archive.StatusArchive;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.ButtonLine;
import io.kurumi.ntt.model.request.ButtonMarkup;
import io.kurumi.ntt.utils.BotLog;
import io.kurumi.ntt.utils.NTT;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NumberUtil;

public class StatusAction extends Fragment {

	static final String POINT_REPLY = "s_reply";
    static final String POINT_RETWEET_STATUS = "s_rt";
    static final String POINT_DESTROY_RETWEET = "s_unrt";
    static final String POINT_DESTROY_STATUS = "s_del";
    static final String POINT_SHOW_FULL = "s_full";
    static final String POINT_LIKE_STATUS = "s_like";
    static final String POINT_UNLIKE_STATUS = "s_ul";
    public static Data<CurrentAccount> current = new Data<CurrentAccount>(CurrentAccount.class);

    public static ButtonMarkup createMarkup(final long accountId,final long statusId,final boolean del,final boolean full,final boolean retweeted,final boolean liked) {

        return new ButtonMarkup() {{

				ButtonLine line = newButtonLine();

				line.newButton("↪",POINT_REPLY,accountId);

				if (retweeted) {

					line.newButton("❎️",POINT_DESTROY_RETWEET,statusId);

				} else {

					line.newButton("🔄",POINT_RETWEET_STATUS,statusId);

				}

				if (liked) {

					line.newButton("💔",POINT_UNLIKE_STATUS,full,retweeted,liked);

				} else {

					line.newButton("❤",POINT_LIKE_STATUS,full,retweeted,liked);

				}

				if (del) {

					line.newButton("❌️",POINT_DESTROY_STATUS);

				}

				if (!full) {

					line.newButton("🔎",POINT_SHOW_FULL,accountId,statusId,1,retweeted ? 1 : 0,liked ? 1 : 0);

				}

				// line.newButton("🔇",POINT_MUTE_USER,status.getUser().getId());


			}};

    }

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerFunction("current");

        registerCallback(
			POINT_REPLY,
			POINT_LIKE_STATUS,
			POINT_UNLIKE_STATUS,
			POINT_RETWEET_STATUS,
			POINT_DESTROY_RETWEET,
			POINT_DESTROY_STATUS,
			POINT_SHOW_FULL);


    }

    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params) {

        requestTwitter(user,msg,true);

    }

    @Override
    public void onTwitterFunction(final UserData user,Msg msg,String function,String[] params,final TAuth account) {

        current.setById(user.id,new CurrentAccount() {{

					id = user.id;

					accountId = account.id;

				}});

        msg.send("当前操作账号已设为 : {} \n当多用户时，可用此命令设置默认账号。",account.archive().urlHtml()).html().exec();

    }

    @Override
    public void onCallback(UserData user,Callback callback,String point,String[] params) {

		InlineKeyboardButton[] buttons = callback.message().replyMarkup().inlineKeyboard()[0];

		params = ArrayUtil.remove(buttons[2].callbackData().split(","),0);

		if (params.length != 3) {

			callback.invalidQuery();

			return;

		}

		long accountId = NumberUtil.parseLong(ArrayUtil.remove(buttons[0].callbackData().split(","),0)[0]);
		long statusId = NumberUtil.parseLong(ArrayUtil.remove(buttons[1].callbackData().split(","),0)[0]);

		boolean isFull = "true".equals(params[0]);
        boolean retweeted = "true".equals(params[1]);
        boolean liked = "true".equals(params[2]);

		if (POINT_REPLY.equals(point)) {

			getInstance(StatusUpdate.class).reply(user,callback,accountId,statusId);

			return;

		}


        long count = TAuth.data.countByField("user",user.id);

        if (count == 0) {

            callback.alert("这需要认证Twitter账号才可使用 :)");

            return;

        }

        TAuth auth = null;

        if (count > 1) {

			if (accountId != -1) {

				auth = TAuth.getById(accountId);

			}

			if (auth == null || !auth.user.equals(user.id)) {

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

			}

		} else {

			auth = TAuth.getByUser(user.id).first();

		}
		
		if (auth == null) {

			callback.alert("你还没有认证账号...");

			return;

		}

		Twitter api = auth.createApi();

		StatusArchive archive = StatusArchive.get(statusId);

		if (archive == null) {

			Status status;

			try {

				status = api.showStatus(statusId);

				StatusArchive.save(status).loop(api);

				liked = status.isFavorited();

				retweeted = status.isRetweetedByMe();

			} catch (TwitterException e) {

				callback.alert(NTT.parseTwitterException(e));

				return;

			}

		}

		if (POINT_LIKE_STATUS.equals(point)) {

			try {

				api.createFavorite(statusId);

				callback.text("已打心 ~");

				liked = true;

			} catch (TwitterException e) {

				liked = true;

				callback.alert(NTT.parseTwitterException(e));

			}

		} else if (POINT_UNLIKE_STATUS.equals(point)) {

			try {

				api.destroyFavorite(statusId);

				liked = false;

				callback.text("已取消打心 ~");

			} catch (TwitterException e) {

				liked = false;

				callback.alert(NTT.parseTwitterException(e));

			}

		} else if (POINT_RETWEET_STATUS.equals(point)) {

			try {

				api.retweetStatus(statusId);

				retweeted = true;

				callback.text("已转推 ~");

			} catch (TwitterException e) {

				retweeted = true;

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

				api.unRetweetStatus(statusId);

				retweeted = false;

				callback.text("已撤销转推 ~");

			} catch (TwitterException e) {

				callback.alert(NTT.parseTwitterException(e));

			}

		} else if (POINT_SHOW_FULL.equals(point)) {

			archive.loop(api);

			if (callback.message().caption() != null) {

				BaseResponse resp = bot().execute(new EditMessageCaption(callback.chatId(),callback.messageId()).caption(archive.toHtml(auth)).parseMode(ParseMode.HTML).replyMarkup(createMarkup(auth.id,archive.id,archive.from.equals(auth.id),true,retweeted,liked).markup()));

				if (!resp.isOk()) {

					BotLog.debug("显示全文失败 :" + resp.errorCode() + " " + resp.description());

				}

			} else {

				callback.edit(archive.toHtml(auth)).buttons(createMarkup(auth.id,archive.id,archive.from.equals(auth.id),true,retweeted,liked)).html().exec();

			}

			callback.text("已展开 ~");

			return;

		}

		callback.editMarkup(createMarkup(auth.id,archive.id,archive.from.equals(auth.id),isFull,retweeted,liked));

	}

    public static class CurrentAccount {

        public long id;

        public long accountId;

    }


}
