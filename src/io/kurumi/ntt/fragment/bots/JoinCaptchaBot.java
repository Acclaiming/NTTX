package io.kurumi.ntt.fragment.bots;

import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.abs.Msg;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Timer;
import com.pengrad.telegrambot.model.User;
import io.kurumi.ntt.fragment.abs.request.ButtonMarkup;
import java.util.TimerTask;
import java.util.Date;
import io.kurumi.ntt.fragment.abs.Callback;

public class JoinCaptchaBot extends BotFragment {

	public Long botId;
    public Long userId;

	public String botToken;
	public String userName;

	@Override
	public void reload() {

		UserBot bot = UserBot.data.getById(botId);

		botToken = bot.token;

		userId = bot.user;

		UserData user = UserData.get(userId);

		if (user == null) {

			userName = "(" + userId + ")";

		} else {

			userName = user.name();

		}

	}

    @Override
    public String botName() {

        return "Join Captcha Bot For " + userName;

    }

	@Override
	public String getToken() {
		
		return botToken;
		
	}

	HashMap<Long,LinkedList<Long>> cache = new HashMap<>();

	static Timer timer = new Timer();

	final String POINT_AUTH = "auth";

	@Override
	public boolean onGroup(UserData user,final Msg msg) {

		if (msg.message().leftChatMember() != null) {

			if (cache.containsKey(msg.chatId().longValue())) {

				LinkedList<Long> group = cache.get(msg.chatId().longValue());

				group.remove(user.id.longValue());

				if (group.isEmpty()) cache.remove(msg.chatId().longValue());

			}

		} else if (msg.message().newChatMember() != null) {

			LinkedList<Long> group = cache.containsKey(msg.chatId().longValue()) ? cache.get(msg.chatId()) : new LinkedList<Long>();

			final User newMember = msg.message().newChatMember();

			if (newMember.isBot()) return false;

			group.add(newMember.id());

			cache.put(msg.chatId().longValue(),group);

			final UserData newData = UserData.get(newMember);

			String[] info = new String[] {

				"你好呀，新加裙的绒布球 " + newData.userName() + " ~\n",

				"现在需要确认一下乃是不是机器人绒布球了 ~\n",

				"来 喵 ~ 180秒以内 注意不要点按钮喵 ~",

			};


			ButtonMarkup buttons = new ButtonMarkup() {{

					newButtonLine("喵喵喵",POINT_AUTH,newMember.id());
					newButtonLine("喵喵喵",POINT_AUTH,newMember.id());
					newButtonLine("喵喵喵",POINT_AUTH,newMember.id());
					newButtonLine("喵喵喵",POINT_AUTH,newMember.id());
					newButtonLine("喵喵喵",POINT_AUTH,newMember.id());

				}};

			setPoint(newData,POINT_AUTH);

			msg.send(info).buttons(buttons).html().exec();
			
			timer.schedule(new TimerTask() {

					@Override
					public void run() {

						LinkedList<Long> group = cache.containsKey(msg.chatId().longValue()) ? cache.get(msg.chatId()) : new LinkedList<Long>();

						if (group.contains(newData.id.longValue())) {

							clearPoint(newData);

							group.remove(newData.id);

							if (group.isEmpty()) {

								cache.remove(msg.chatId().longValue());

							} else {

								cache.put(msg.chatId().longValue(),group);

							}

							if (msg.kick(newData.id)) {

								msg.send(newData.userName() + " 不理解喵喵的语言 , 真可惜喵...").exec();

							}

						}

					}

				},new Date(System.currentTimeMillis() + 180 * 60 * 1000));

		}

		return false;

	}

	@Override
	public boolean onCallback(UserData user,Callback callback) {

		long target = Long.parseLong(callback.params[1]);

		if (!user.id.equals(target)) {

			callback.alert("这个验证不针对乃 ~");

		} else {

			LinkedList<Long> group = cache.containsKey(callback.chatId().longValue()) ? cache.get(callback.chatId()) : new LinkedList<Long>();

			if (group.contains(callback.chatId())) {

				group.remove(user.id);

				if (group.isEmpty()) {

					cache.remove(callback.chatId().longValue());

				} else {

					cache.put(callback.chatId().longValue(),group);

				}

			}

			clearPoint(user);

			if (callback.kick()) {

				callback.send(user.userName() + " 瞎按按钮 , 真可惜喵...").exec();

			} else {

				callback.send("机器人权限不足... 已退出").exec();

				callback.exit();

			}

		}

		return true;

	}

	@Override
	public boolean onPointedGroup(UserData user,Msg msg) {
		
		LinkedList<Long> group = cache.containsKey(msg.chatId().longValue()) ? cache.get(msg.chatId()) : new LinkedList<Long>();

		if (group.contains(msg.chatId())) {

			group.remove(user.id);

			if (group.isEmpty()) {

				cache.remove(msg.chatId().longValue());

			} else {

				cache.put(msg.chatId().longValue(),group);

			}

		}

		clearPoint(user);

		if (msg.hasText() && msg.text().contains("喵")) {

			msg.send(user.userName() + " 通过了图灵(划掉)验证 ~").exec();

		} else if (msg.kick()) {

			msg.send(user.userName() + " 不懂喵喵的语言 , 真可惜喵...").exec();

		} else {

			msg.send("机器人权限不足... 已退出").exec();

			msg.exit();


		}

		return true;

	}

}
