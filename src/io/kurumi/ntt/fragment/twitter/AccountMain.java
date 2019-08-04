package io.kurumi.ntt.fragment.twitter;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.db.UserData;
import cn.hutool.core.util.NumberUtil;
import io.kurumi.ntt.utils.Html;
import io.kurumi.ntt.model.request.ButtonMarkup;
import io.kurumi.ntt.fragment.twitter.auto.AutoMain;
import io.kurumi.ntt.fragment.twitter.track.TrackMain;
import io.kurumi.ntt.fragment.twitter.status.TimelineMain;

public class AccountMain extends Fragment {

	public static final String POINT_ACCOUNT = "twi_show";

	final String POINT_EXPORT = "twi_export";
	final String POINT_LOGOUT = "twi_logout";
	final String POINT_LOGOUT_CONFIRM = "twi_logout_confim";

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerCallback(POINT_ACCOUNT,POINT_EXPORT,POINT_LOGOUT,POINT_LOGOUT_CONFIRM);

		origin.addFragment(new AutoMain());
		origin.addFragment(new TrackMain());
		origin.addFragment(new TimelineMain());

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

		if (POINT_ACCOUNT.equals(point)) {

			accountMain(user,callback,account);

		} else if (POINT_LOGOUT.equals(point)) {

			accountLogout(user,callback,account);

		} else if (POINT_EXPORT.equals(point)) {

			accountExport(user,callback,account);

		}

	}

	void accountMain(UserData user,Callback callback,TAuth account) {

		String message = "功能选单 : " + Html.b("User") + " [ " + Html.code(account.id) + " ]";

		message += "\n\nName : " + account.archive().name;

		message += "\nSN : " + Html.code("@" + account.archive().screenName);

		ButtonMarkup functions = new ButtonMarkup();

		functions.newButtonLine("自动处理 >>",AutoMain.POINT_AUTO,account.id);
		functions.newButtonLine("通知 >>",TrackMain.POINT_TRACK,account.id);
		functions.newButtonLine("推文流 >>",TimelineMain.POINT_TL,account.id);

		functions.newButtonLine()
			.newButton("导出",POINT_EXPORT,account.id)
			.newButton("移除",POINT_LOGOUT,account.id);

		functions.newButtonLine("🔙",TwitterMain.POINT_BACK);

		callback.edit(message).buttons(functions).html().async();

	}

	void accountExport(UserData user,Callback callback,TAuth account) {

		String message = "认证信息 [ " + account.archive().name + " ]";

        message += "\n\n" + Html.b("Consumer Key") + " : " + Html.code(account.apiKey);
        message += "\n\n" + Html.b("Consumer Key Secret") + " : " + Html.code(account.apiKeySec);
        message += "\n\n" + Html.b("Access Token") + " : " + Html.code(account.accToken);
        message += "\n\n" + Html.b("Access Token Secret") + " : " + Html.code(account.accTokenSec);

        ButtonMarkup back = new ButtonMarkup();

		back.newButtonLine("🔙",POINT_ACCOUNT,account.id);

		callback.edit(message).buttons(back).html().async();

	}

	void accountLogout(UserData user,Callback callback,TAuth account) {

		String message = "点击来确认移除你的账号 [ " + account.archive().name + " ]\n\n服务器端记录会被完全删除 , 但 Twitter 中的会话管理中仍会显示NTT , 在会话管理中移除NTT使导出功能导出的认证失效 .";

		ButtonMarkup logout = new ButtonMarkup();

		logout.newButtonLine("删除",POINT_LOGOUT_CONFIRM,account.id);

		logout.newButtonLine("🔙",POINT_ACCOUNT,account.id);

		callback.edit(message).buttons(logout).async();

	}

	void confirmLogout(UserData user,Callback callback,TAuth account) {

		TAuth.data.deleteById(account.id);

		callback.alert("好. 账号数据已删除.");

		getInstance(TwitterMain.class).mainMenu(user,callback,true);

	}

}
