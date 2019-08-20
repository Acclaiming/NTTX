package io.kurumi.ntt.fragment.group.options;

import cn.hutool.core.util.NumberUtil;
import io.kurumi.ntt.db.GroupData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.bots.GroupBot;
import io.kurumi.ntt.fragment.group.GroupAdmin;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.ButtonMarkup;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.Html;
import io.kurumi.ntt.utils.NTT;

public class OptionsMain extends Fragment {

	@Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerFunction("options");

        registerCallback(POINT_OPTIONS);

        registerPayload(PAYLOAD_OPTIONS);

		origin.addFragment(new ServiceMain());
		origin.addFragment(new RestMain());
		origin.addFragment(new CaptchaMain());
		origin.addFragment(new WelcomeMain());
		origin.addFragment(new SpamMain());
		origin.addFragment(new LogMain());
		
    }

    @Override
    public int checkFunctionContext(UserData user,Msg msg,String function,String[] params) {

        return FUNCTION_GROUP;

    }

    public static final String POINT_OPTIONS = "group_options";

    final String PAYLOAD_OPTIONS = "go";

	public static String doc = Html.b("注意 : ") + "使用前请阅读 " + Html.a("文档","https://manual.kurumi.io/group");

	private String optionsMessage(GroupData data) {

		String message = Html.b(data.title);

		message += "\n" + Html.i("更改群组的设定");

		message += "\n\n" + doc;

		return message;

	}

	@Override
    public void onFunction(UserData user,final Msg msg,String function,String[] params) {

        if (user.blocked()) {

            msg.send("你不能这么做 (为什么？)").failedWith();

            return;

        }

        final GroupData data = GroupData.get(msg.chat());

        if (!NTT.isGroupAdmin(this,msg.chatId(),user.id)) {

            msg.reply("你不是绒布球").failedWith();

            return;

        }

        if (data.full_admins != null && data.not_trust_admin != null) {

            if ((!(origin instanceof GroupBot) || !((GroupBot) origin).userId.equals(user.id)) && !data.full_admins.contains(user.id)) {

                msg.reply("根据群组设定，你不可以更改群组选项 , 除非本群组没有群主与全权限管理员").send();

                return;

            }

        }

        if (!NTT.isGroupAdmin(msg.fragment,msg.chatId(),origin.me.id())) {

            msg.reply("BOT不是群组管理员 :)").async();

            return;

        }

        if (!NTT.isUserContactable(this,user.id)) {

            ButtonMarkup buttons = new ButtonMarkup();

            buttons.newButtonLine("打开",POINT_OPTIONS,user.id);

            msg.reply("点击按钮在私聊打开设置面板 :)\n\n如果没有反应 请检查是否停用了BOT (私聊内点击 '取消屏蔽' 解除) 然后重新点击下方 '打开' 按钮 ~").buttons(buttons).async();

            return;

        }

        new Send(this,user.id,optionsMessage(data)).buttons(menuMarkup(data)).html().async();

        msg.reply("已经通过私聊发送群组设置选项").failedWith();

    }

	@Override
    public void onPayload(UserData user,Msg msg,String payload,String[] params) {

        long groupId = NumberUtil.parseLong(params[0]);

        if (!GroupAdmin.fastAdminCheck(this,groupId,user.id,false)) {

            msg.reply("你不是该群组的管理员 如果最近半小时更改 请在群组中使用 /update_admins_cache 更新缓存.");

            return;

        }

        final GroupData data = GroupData.get(groupId);

        msg.send(optionsMessage(data)).buttons(menuMarkup(data)).html().exec();


    }

    @Override
    public void onCallback(UserData user,Callback callback,String point,String[] params) {

        if (POINT_OPTIONS.equals(point)) {

			long id = NumberUtil.parseLong(params[0]);

			if (callback.isGroup()) {

				if (user.id.equals(id)) {

					callback.url("https://t.me/" + origin.me.username() + "?start=" + PAYLOAD_OPTIONS + PAYLOAD_SPLIT + callback.chatId() + PAYLOAD_SPLIT + user.id);

				} else {

					callback.alert("你不是绒布球 :)");

				}

			} else {

				final GroupData data = GroupData.get(id);

				callback.edit(optionsMessage(data)).buttons(menuMarkup(data)).html().async();

			}

		}

	}

	ButtonMarkup menuMarkup(final GroupData data) {

        return new ButtonMarkup() {{

				newButtonLine("🛠️  功能 选项",ServiceMain.POINT_SERVICE,data.id);
				newButtonLine("📝  成员 限制",RestMain.POINT_REST,data.id);
				newButtonLine("🚪  加群 验证",RestMain.POINT_REST,data.id);
				newButtonLine("📢  欢迎 消息",WelcomeMain.POINT_WELCOME,data.id);
				newButtonLine("🔎  Anti Spam",SpamMain.POINT_SPAM,data.id);
				newButtonLine("🎥  日志 记录",LogMain.POINT_LOG,data.id);


			}};


    }

}
