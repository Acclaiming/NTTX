package io.kurumi.ntt.fragment.group.options;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.http.HtmlUtil;
import io.kurumi.ntt.db.GroupData;
import io.kurumi.ntt.db.PointData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.request.ButtonMarkup;

public class SpamMain extends Fragment {

	public static String POINT_SPAM = "group_spam";

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerCallback(POINT_SPAM);

	}

	@Override
	public void onCallback(UserData user,Callback callback,String point,String[] params) {

		if (params.length == 0 || !NumberUtil.isNumber(params[0])) {

			callback.invalidQuery();

			return;

		}

        final GroupData data = GroupData.data.getById(NumberUtil.parseLong(params[0]));

		if (data == null) {

			callback.invalidQuery();

			return;

		}

		if (params.length == 1) {

			String message = "群组反垃圾用户功能选单 (Anti Spam)";

			message += "\n\n" + OptionsMain.doc;

			callback.edit(message).buttons(spamMenu(user,data)).html().async();

			return;

		}

		if ("anti_halal".equals(params[1])) {

			if (data.anti_halal == null) {

				data.anti_halal = true;

				callback.text("🔎 已开启");

			} else {

				data.anti_halal = null;

				callback.text("🔎️  已关闭");

			}


		} else if ("cas".equals(params[1])) {

			if (data.cas_spam == null) {

				data.cas_spam = true;

				callback.text("🔎 已开启");

			} else {

				data.cas_spam = null;

				callback.text("🔎️  已关闭");

			}

		} else if ("backhole".equals(params[1])) {

			if (data.backhole == null) {

				data.backhole = true;

				callback.alert("警告 : 如果你不知道你自己干什么，请关闭 '黑箱'！");

			} else {

				data.backhole = null;

				callback.text("🔎️  已关闭");

			}


		}

		callback.editMarkup(spamMenu(user,data));

	}


	ButtonMarkup spamMenu(final UserData user,final GroupData data) {

        return new ButtonMarkup() {{

				newButtonLine()
                    .newButton("反清真")
                    .newButton(data.anti_halal != null ? "✅" : "☑",POINT_SPAM,data.id,"anti_halal");

				newButtonLine()
                    .newButton("CAS")
                    .newButton(data.cas_spam != null ? "✅" : "☑",POINT_SPAM,data.id,"cas");

				if (user.admin() || data.backhole != null) {

					newButtonLine()
						.newButton("黑箱")
						.newButton(data.backhole != null ? "✅" : "☑",POINT_SPAM,data.id,"backhole");

				}

				newButtonLine("🔙",OptionsMain.POINT_OPTIONS,data.id);

			}};

    }

}

