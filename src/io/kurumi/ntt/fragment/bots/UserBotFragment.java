package io.kurumi.ntt.fragment.bots;

import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import java.util.Map;
import io.kurumi.ntt.model.Msg;

public class UserBotFragment extends BotFragment {

	private UserBot bot;

	private Long botId;
	private String userName;
    private String botToken;

	private Long userId;

	public Map<String,Object> params;

	public UserBotFragment(Long botId) {

		this.botId = botId;

	}

	@Override
	public void reload() {

		super.reload();

		bot = UserBot.data.getById(botId);

		botId = bot.id;
		userName = bot.userName;
        botToken = bot.token;
        userId = bot.user;

		params = bot.params;

	}

	public UserData getOwner() {

		return UserData.get(userId);

	}

	@Override
	public String botName() {

		return getClass().getSimpleName();

	}

	public <T> T getParams(String key) {

		return (T)params.get(key);

	}

	public void setParams(String key,Object value) {

		params.put(key,value);

	}

	public void save() {

		UserBot.data.setById(botId,bot);

	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		if (!msg.isPrivate()) return;

		if (!"start".equals(function)) return;

		if (!user.admin() || !user.id.equals(userId)) return;

	}

	@Override
	public void stop() {

		save();

		super.stop();

	}

}
