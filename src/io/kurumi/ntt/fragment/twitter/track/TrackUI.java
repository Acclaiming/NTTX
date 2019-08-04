package io.kurumi.ntt.fragment.twitter.track;

import cn.hutool.core.util.NumberUtil;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.AccountMain;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.request.ButtonMarkup;

public class TrackUI extends Fragment {

	public static final String POINT_TRACK = "twi_track";

    final String POINT_SETTING_FOLLOWERS = "twi_fo";
    final String POINT_SETTING_FOLLOWERS_INFO = "twi_fo_info";
    final String POINT_SETTING_FOLLOWINGS_INFO = "twi_fr_info";

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerCallback(
			POINT_TRACK,
			POINT_SETTING_FOLLOWERS,
			POINT_SETTING_FOLLOWINGS_INFO,
			POINT_SETTING_FOLLOWERS_INFO);

    }

	@Override
	public void onCallback(UserData user,Callback callback,String point,String[] params) {

		if (params.length == 0 || !NumberUtil.isNumber(params[0])) return;

		long accountId = NumberUtil.parseLong(params[0]);

		TAuth account = TAuth.getById(accountId);

		if (account == null) {

			callback.alert("无效的账号 .");

			callback.delete();

			return;

		}

		if (POINT_TRACK.equals(point)) {

			autoMain(user,callback,account);

		} else {

			setConfig(user,callback,point,account);

		}

	}

	void autoMain(UserData user,Callback callback,TAuth account) {

		String message = "账号通知设置选单 : [ " + account.archive().name + " ]";

		ButtonMarkup config = new ButtonMarkup();

		config.newButtonLine()
			.newButton("关注者变化")
			.newButton(account.fo != null ? "✅" : "☑",POINT_SETTING_FOLLOWERS,account.id);

		config.newButtonLine()
			.newButton("关注中账号更改")
			.newButton(account.fr_info != null ? "✅" : "☑",POINT_SETTING_FOLLOWINGS_INFO,account.id);

		config.newButtonLine()
			.newButton("关注者账号更改")
			.newButton(account.fo_info != null ? "✅" : "☑",POINT_SETTING_FOLLOWERS_INFO,account.id);

		config.newButtonLine("🔙",AccountMain.POINT_ACCOUNT,account.id);

		callback.edit(message).buttons(config).async();

	}

	void setConfig(UserData user,Callback callback,String point,TAuth account) {

		if (POINT_SETTING_FOLLOWERS.equals(point)) {

			if (account.fo == null) {

				account.fo = true;

				callback.text("✅ 已开启");

			} else {

				account.fo = null;

				callback.text("✅ 已关闭");

			}

		} else if (POINT_SETTING_FOLLOWERS_INFO.equals(point)) {

			if (account.fo_info == null) {

				account.fo_info = true;

				callback.text("✅ 已开启");

			} else {

				account.fo_info = null;

				callback.text("✅ 已关闭");

			}

		} else if (POINT_SETTING_FOLLOWINGS_INFO.equals(point)) {

			if (account.fr_info == null) {

				account.fr_info = true;

				callback.text("✅ 已开启");

			} else {

				account.fr_info = null;

				callback.text("✅ 已关闭");

			}

		}

		TAuth.data.setById(account.id,account);

		autoMain(user,callback,account);

	}


}
