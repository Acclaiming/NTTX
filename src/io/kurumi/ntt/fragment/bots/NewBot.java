package io.kurumi.ntt.fragment.bots;

import io.kurumi.ntt.db.PointStore;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.funcs.abs.Function;
import io.kurumi.ntt.model.Msg;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import io.kurumi.ntt.Env;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.GetMe;
import com.pengrad.telegrambot.response.GetMeResponse;
import io.kurumi.ntt.model.request.Keyboard;

public class NewBot extends Function {

	@Override
	public void functions(LinkedList<String> names) {

		names.add("newbot");

	}

	static class CreateBot {

		int progress = 0;

		UserBot bot;

		int type = -1;

	}

	final String POINT_CREATE_BOT = "bot.create";

	@Override
	public void points(LinkedList<String> points) {
		
		points.add(POINT_CREATE_BOT);
		
	}

	@Override
	public int target() {
		
		return Private;
		
	}
	
	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		msg.send("现在请输入BotToken :","","BotToken可以当成TelegramBot登录的账号密码、需要在 @BotFather 申请。").exec();

		setPoint(user,POINT_CREATE_BOT,new CreateBot());

	}

	@Override
	public void onPoint(UserData user,Msg msg,PointStore.Point point) {

		if (POINT_CREATE_BOT.equals(point.point)) {

			CreateBot data = (CreateBot) point.data;

			if (data.progress == 0) {

				if (!msg.hasText() ||  !msg.text().contains(":")) {

					msg.send("无效的Token.请重试. ","Token 看起来像这样: '12345678:ABCDEfgHIDUROVjkLmNOPQRSTUvw-cdEfgHI'","或使用 /cancel 取消创建BOT").exec();

					return;

				}

				msg.send("正在检查BOT信息...").exec();

				GetMeResponse me = new TelegramBot(msg.text()).execute(new GetMe());

				if (!me.isOk()) {
					
					msg.send("Token无效...").exec();
					
					return;
					
				}
				
				UserBot bot = new UserBot();
				
				bot.id = me.user().id();
				bot.user = user.id;
				bot.userName = me.user().username();
				bot.token = msg.text();
				
				data.bot = bot;
				
				data.progress = 1;
				
				msg.send("现在选择BOT类型 :").keyboard(new Keyboard() {{
					
					newButtonLine("转发私聊");
					
					newButtonLine("取消创建");
					
				}}).exec();
				
			} else if (data.progress == 1) {
				
				if ("取消创建".equals(msg.text())) {
					
					clearPoint(user);
					
					msg.send("已经取消 ~").removeKeyboard().exec();
					
				} else if ("转发私聊".equals(msg.text())) {
					
					data.bot.type = 0;
					
					data.progress = 10;
					
					msg.send("好，请发送私聊BOT的欢迎语，这将在 /start 时发送").exec();
					msg.send("就像这样 : 直接喵喵就行了 ~").exec();
					
				} else {
					
					msg.send("你正在创建BOT，请在下方键盘选择","或使用 /cancel 取消").exec();
					
				}
				
			} else if (data.progress == 10) {
				
				if (!msg.hasText()) {
					
					msg.send("你正在创建私聊BOT，请发送欢迎语","或使用 /cancel 取消").exec();
					
					return;
					
				}
				
				clearPoint(user);
				
				msg.send("创建成功... 正在启动").exec();
				
				data.bot.params.put("msg",msg.text());
				
				UserBot.data.setById(data.bot.id,data.bot);
				
				data.bot.startBot();
				
				msg.send("启动成功！ 你的BOT : " + data.bot.userName,"不要忘记给BOT发一条信息 这样BOT才能转发信息给你","现在你可以使用 /mybots 修改或删除这只BOT了 ~").exec();
				
			}

		}

	}

}
