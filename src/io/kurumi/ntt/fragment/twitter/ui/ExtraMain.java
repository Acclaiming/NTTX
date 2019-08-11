package io.kurumi.ntt.fragment.twitter.ui;

import cn.hutool.core.util.NumberUtil;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.request.ButtonMarkup;
import io.kurumi.ntt.fragment.twitter.ui.extra.OWUnfoPublish;
import io.kurumi.ntt.fragment.twitter.ui.extra.SpamMain;

public class ExtraMain extends Fragment {

	public static String POINT_EXTRA = "twi_ex";

	public static String POINT_EXTRA_SET = "twi_ex_set";

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerCallback(POINT_EXTRA);

		origin.addFragment(new OWUnfoPublish());
		origin.addFragment(new SpamMain());
		
	}

	@Override
	public void onCallback(UserData user,Callback callback,String point,String[] params) {

		if (params.length == 0 || !NumberUtil.isNumber(params[0])) {

			callback.invalidQuery();

			return;

		}

		long accountId = NumberUtil.parseLong(params[0]);

		TAuth account = TAuth.getById(accountId);

		if (account == null) {

			callback.alert("无效的账号 .");

			callback.delete();

			return;

		}

		if (POINT_EXTRA.equals(point)) {

			extraMain(user,callback,account);

		}

	}

	void extraMain(UserData user,Callback callback,TAuth account) {

		String message = "实验性功能选单 : [ " + account.archive().name + " ]";

		message += "\n\n ( 已发布但不适合加入标准的功能 )";

		ButtonMarkup buttons = new ButtonMarkup();

		buttons.newButtonLine("单向取关推送 >>",OWUnfoPublish.POINT_OUP,account.id);

		if (user.admin()) {

			buttons.newButtonLine("联合封禁 >>",SpamMain.POINT_SPAM,account.id);

		}

		buttons.newButtonLine("🔙",AccountMain.POINT_ACCOUNT,account.id);

		callback.edit(message).buttons(buttons).async();

	}



}
