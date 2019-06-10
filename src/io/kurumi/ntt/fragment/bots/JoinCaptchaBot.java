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

		if (bot == null) return;
		
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

	HashMap<Long,HashMap<Long,Msg>> cache = new HashMap<>();

	static Timer timer = new Timer();

	final String POINT_AUTH = "auth";

	@Override
	public boolean onGroup(UserData user,final Msg msg) {

		if (msg.message().groupChatCreated() != null || msg.message().supergroupChatCreated() != null) {
			
			msg.send("欢迎使用由 @NTT_X 驱动的加群验证BOT 给与 删除消息 和 封禁用户 权限就可以使用了 ~").exec();
			
		} else if (msg.message().leftChatMember() != null) {

			if (cache.containsKey(msg.chatId().longValue())) {

				HashMap<Long, Msg> group = cache.get(msg.chatId().longValue());

				if (group.containsKey(user.id.longValue())) {
				
				group.remove(user.id.longValue()).delete();

				if (group.isEmpty()) cache.remove(msg.chatId().longValue());
				
				}

			}

		} else if (msg.message().newChatMember() != null || msg.message().newChatMembers() != null) {

			final HashMap<Long, Msg> group = cache.containsKey(msg.chatId().longValue()) ? cache.get(msg.chatId()) : new HashMap<Long,Msg>();

			User newMember = msg.message().newChatMember();

			if (newMember == null) newMember = msg.message().newChatMembers()[0];
			
			if (newMember.isBot()) return false;

			
			final UserData newData = UserData.get(newMember);

			String[] info = new String[] {

				"你好呀，新加裙的绒布球 " + newData.userName() + " ~\n",

				"现在需要确认一下乃是不是机器人绒布球了 ~\n",

				"发送 喵 就可以通过验证了 ~ 3分钟以内呀 (๑˃̵ᴗ˂̵)و \n",
				
				"注意不要点按钮 喵 ~"

			};


			ButtonMarkup buttons = new ButtonMarkup() {{

					newButtonLine()
					.newButton("不要",POINT_AUTH,newData.id)
					.newButton("点",POINT_AUTH,newData.id)
					.newButton("按钮",POINT_AUTH,newData.id)
					.newButton("喵",POINT_AUTH,newData.id);
					// newButtonLine("喵喵喵",POINT_AUTH,newData.id);

				}};

			setPoint(newData,POINT_AUTH);

			group.put(newMember.id(),msg.send(info).buttons(buttons).html().send());

			cache.put(msg.chatId().longValue(),group);
	
			timer.schedule(new TimerTask() {

					@Override
					public void run() {

						final HashMap<Long, Msg> group = cache.containsKey(msg.chatId().longValue()) ? cache.get(msg.chatId()) : new HashMap<Long,Msg>();

						if (group.containsKey(newData.id.longValue())) {

							clearPoint(newData);

							group.remove(newData.id).delete();

							if (group.isEmpty()) {

								cache.remove(msg.chatId().longValue());

							} else {

								cache.put(msg.chatId().longValue(),group);

							}

							if (msg.kick(newData.id)) {

								msg.send(newData.userName() + " 不理解喵喵的语言 , 真可惜喵...").html().exec();

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

			HashMap<Long, Msg> group = cache.containsKey(callback.chatId().longValue()) ? cache.get(callback.chatId()) : new HashMap<Long,Msg>();

			if (group.containsKey(user.id)) {

				group.remove(user.id).delete();

				if (group.isEmpty()) {

					cache.remove(callback.chatId().longValue());

				} else {

					cache.put(callback.chatId().longValue(),group);

				}

			}

			clearPoint(user);

			if (callback.kick()) {

				callback.send(user.userName() + " 瞎按按钮 , 真可惜喵...").html().exec();

			} else {

				callback.send("机器人权限不足... 已退出").exec();

				callback.exit();

			}

		}

		return true;

	}

	@Override
	public boolean onPointedGroup(UserData user,Msg msg) {
		
		HashMap<Long, Msg> group = cache.containsKey(msg.chatId().longValue()) ? cache.get(msg.chatId()) : new HashMap<Long,Msg>();

		if (group.containsKey(user.id)) {

			msg.delete();
			
			group.remove(user.id).delete();

			if (group.isEmpty()) {

				cache.remove(msg.chatId().longValue());

			} else {

				cache.put(msg.chatId().longValue(),group);

			}

		}

		clearPoint(user);

		if (msg.hasText() && msg.text().contains("喵")) {

			msg.send(user.userName() + " 通过了图灵(划掉)验证 ~").html().exec();

		} else if (msg.kick()) {

			msg.send(user.userName() + " 不懂喵喵的语言 , 真可惜喵...").html().exec();

		} else {

			msg.send("机器人权限不足... 已退出").exec();

			msg.exit();


		}

		return true;

	}

}
