package io.kurumi.ntt.fragment.bots;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.GetMe;
import com.pengrad.telegrambot.response.GetMeResponse;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.Keyboard;
import java.util.HashMap;

public class NewBot extends Fragment {

    final String POINT_CREATE_BOT = "bot.create";

	@Override
	public void init(BotFragment origin) {
	
		super.init(origin);
		
		registerFunction("newbot");

		registerPoint(POINT_CREATE_BOT);

    }

	@Override
	public int checkFunction() {
		
		return FUNCTION_PRIVATE;
		
	}

    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params) {

        msg.send("现在请输入BotToken :", "", "BotToken可以当成TelegramBot登录的账号密码、需要在 @BotFather 申请。").withCancel().exec();

        setPrivatePointData(user, POINT_CREATE_BOT, new CreateBot());

    }

	@Override
	public void onPoint(UserData user,Msg msg,String point,Object data) {
		
        if (POINT_CREATE_BOT.equals(point)) {

            CreateBot create = (CreateBot) data;

            if (create.progress == 0) {

                if (!msg.hasText() || !msg.text().contains(":")) {

                    msg.send("无效的Token.请重试. ", "Token 看起来像这样: '12345678:ABCDEfgHIDUROVjkLmNOPQRSTUvw-cdEfgHI'").withCancel().exec();

                    return;

                }

                msg.send("正在检查BOT信息...").exec();

                GetMeResponse me = new TelegramBot(msg.text()).execute(new GetMe());

                if (!me.isOk()) {

                    msg.send("Token无效...").withCancel().exec();

                    return;

                }

                UserBot bot = new UserBot();

                bot.id = me.user().id();
                bot.user = user.id;
                bot.userName = me.user().username();
                bot.token = msg.text();

                create.bot = bot;

                create.progress = 1;

                msg.send("现在选择BOT类型 :").keyboard(new Keyboard() {{

                    newButtonLine("转发私聊");

                    newButtonLine("加群验证");

                    newButtonLine("取消创建");

                }}).exec();

            } else if (create.progress == 1) {

                if ("取消创建".equals(msg.text())) {

                    clearPrivatePoint(user);

                    msg.send("已经取消 ~").removeKeyboard().exec();

                } else if ("转发私聊".equals(msg.text())) {

                    create.bot.type = 0;

                    create.progress = 10;

                    msg.send("好，请发送私聊BOT的欢迎语，这将在 /start 时发送").removeKeyboard().exec();
                    msg.send("就像这样 : 直接喵喵就行了 ~").withCancel().exec();

                } else if ("加群验证".equals(msg.text())) {

                    create.bot.type = 1;

                    clearPrivatePoint(user);

                    msg.send("创建成功... 正在启动").removeKeyboard().exec();

                    create.bot.params = new HashMap<>();

                    UserBot.data.setById(create.bot.id, create.bot);

                    create.bot.startBot();

                    msg.send("你的BOT : @" + create.bot.userName, "\n将BOT加入群组并设为管理员即可 ~", "\n现在你可以使用 /mybots 修改或删除这只BOT了 ~").exec();


                } else {

                    msg.send("你正在创建BOT，请在下方键盘选择").withCancel().exec();

                }

            } else if (create.progress == 10) {

                if (!msg.hasText()) {

                    msg.send("你正在创建私聊BOT，请发送欢迎语").withCancel().exec();

                    return;

                }

                clearPrivatePoint(user);

                msg.send("创建成功... 正在启动").exec();

                create.bot.params = new HashMap<>();
                create.bot.params.put("msg", msg.text());

                UserBot.data.setById(create.bot.id, create.bot);

                create.bot.startBot();

                msg.send("你的BOT : @" + create.bot.userName, "\n不要忘记给BOT发一条信息 这样BOT才能转发信息给你 ~", "\n现在你可以使用 /mybots 修改或删除这只BOT了 ~").exec();

            }

        }

    }

    static class CreateBot {

        int progress = 0;

        UserBot bot;

        int type = -1;

    }

}
