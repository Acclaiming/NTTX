package io.kurumi.ntt.fragment.twitter.auto;

import cn.hutool.core.util.NumberUtil;
import io.kurumi.ntt.db.Data;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.request.ButtonMarkup;
import twitter4j.AccountSettings;
import io.kurumi.ntt.fragment.twitter.AccountMain;

public class AutoMain extends Fragment {

	public static final String POINT_AUTO = "twi_auto";

    final String POINT_SETTING_MRT = "twi_mrt";
    final String POINT_SETTING_FOBACK = "twi_foback";

    public void init(BotFragment origin) {

        super.init(origin);

        registerCallback(POINT_AUTO,POINT_SETTING_MRT,POINT_SETTING_FOBACK);

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

		if (POINT_AUTO.equals(point)) {

			autoMain(user,callback,account);

		} else {
			
			setConfig(user,callback,point,account);
			
		}

	}

	void autoMain(UserData user,Callback callback,TAuth account) {

		String message = "自动处理设置选单 : [ " + account.archive().name + " ]";

		ButtonMarkup config = new ButtonMarkup();

		config.newButtonLine()
			.newButton("屏蔽新关注中的转推")
			.newButton(account.mrt != null ? "✅" : "☑",POINT_SETTING_MRT,account.id);

		config.newButtonLine()
			.newButton("自动关注新关注者")
			.newButton(account.fb != null ? "✅" : "☑",POINT_SETTING_FOBACK,account.id);
		
		config.newButtonLine("🔙",AccountMain.POINT_ACCOUNT,account.id);
		
		callback.edit(message).buttons(config).async();

	}
	
	void setConfig(UserData user,Callback callback,String point,TAuth account) {
		
		if (POINT_SETTING_MRT.equals(point)) {
			
			if (account.mrt == null) {
				
				account.mrt = true;
				
				callback.text("✅ 已开启");
				
			} else {
				
				account.mrt = null;
				
				callback.text("✅ 已关闭");
				
			}
			
		} else if (POINT_SETTING_FOBACK.equals(point)) {

			if (account.fb == null) {

				account.fb = true;

				callback.text("✅ 已开启");

			} else {

				account.fb = null;

				callback.text("✅ 已关闭");

			}

		}
		
		TAuth.data.setById(account.id,account);
		
		autoMain(user,callback,account);
		
	}

}
