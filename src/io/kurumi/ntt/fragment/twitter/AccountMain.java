package io.kurumi.ntt.fragment.twitter;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.db.UserData;
import cn.hutool.core.util.NumberUtil;
import io.kurumi.ntt.utils.Html;
import io.kurumi.ntt.model.request.ButtonMarkup;

public class AccountMain extends Fragment {
	
	static final String POINT_ACCOUNT = "twi_show";

	final String POINT_LOGOUT = "twi_logout";
	final String POINT_LOGOUT_CONFIRM = "twi_logout_confim";
	
	@Override
	public void init(BotFragment origin) {
		
		super.init(origin);
		
		registerCallback(POINT_ACCOUNT);
		
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
			
		}
		
	}

	void accountMain(UserData user,Callback callback,TAuth account) {
		
		String message = "User [ " + Html.code(account.id) + " ]";
		
		message += "\nName : " + Html.code(account.archive().name);
		
		message += "\nSN : " + Html.b(account.archive().screenName);
		
		ButtonMarkup functions = new ButtonMarkup();
		
		functions.newButtonLine("移除账号",POINT_LOGOUT,account.id);
		
		functions.newButtonLine("🔙",TwitterMain.POINT_BACK);
		
	}
	
	void accountLogout(UserData user,Callback callback,TAuth account) {
		
		String message = "点击来确认移除你的账号 [ " + account.archive().name + " ] , 服务器端记录会被完全删除 , 但 Twitter 中的会话管理中仍会显示NTT , 在会话管理中移除NTT使导出功能导出的认证失效 .";
		
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
