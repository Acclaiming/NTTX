package io.kurumi.ntt.fragment.twitter.ui;

import io.kurumi.ntt.fragment.twitter.ui.extra.*;

import cn.hutool.core.util.NumberUtil;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.ui.AccountMain;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.request.ButtonMarkup;

public class CleanMain extends Fragment {
	
	public static String POINT_CLEAN = "twi_cl";
	
	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerCallback(POINT_CLEAN);

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

		if (params.length == 1) {

			cleanMain(user,callback,account);

		}

	}

	void cleanMain(UserData user,Callback callback,TAuth account) {

		String message = "账号清理 : [ " + account.archive().name + " ]";

		ButtonMarkup buttons = new ButtonMarkup();

		buttons.newButtonLine("清理正在关注 >>",FollowedBy.POINT_FB,account.id);
		buttons.newButtonLine("清理关注者 >>",OWUnfoPublish.POINT_OUP,account.id);
		buttons.newButtonLine("清理静音屏蔽 >>",BlockedBy.POINT_BB,account.id);

		buttons.newButtonLine("🔙",AccountMain.POINT_ACCOUNT,account.id);

		callback.edit(message).buttons(buttons).async();

	}
	
}
