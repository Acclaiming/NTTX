package io.kurumi.ntt.fragment.mstd.ui;

import com.sys1yagi.mastodon4j.api.Scope;
import com.sys1yagi.mastodon4j.api.entity.Account;
import com.sys1yagi.mastodon4j.api.entity.auth.AccessToken;
import com.sys1yagi.mastodon4j.api.entity.auth.AppRegistration;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import io.kurumi.ntt.db.PointData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.mstd.MstdApi;
import io.kurumi.ntt.fragment.mstd.MstdApp;
import io.kurumi.ntt.fragment.mstd.MstdAuth;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.ButtonMarkup;
import io.kurumi.ntt.utils.Html;

public class MsMain extends Fragment {
	
	public static final String POINT_MS_MAIN = "ms_main";

	final String POINT_LOGIN = "ms_login";
	final String POINT_LOGOUT = "ms_logout";
	
	void msMain(UserData user,Msg msg,boolean edit) {
		
		String message;
		
		ButtonMarkup buttons = new ButtonMarkup();
		
		if (MstdAuth.data.containsId(user.id)) {
			
			message = "查看你的账号 ~";
			
			buttons.newButtonLine("移除账号",POINT_LOGOUT);
			
		} else {
			
			message = "还没有认证账号 :)";
			
			buttons.newButtonLine("认证账号",POINT_LOGIN);
			
		}
		
		msg.sendOrEdit(edit,message).buttons(buttons).async();
		
	}
	
	class LoginMstd extends PointData {

		Callback origin;
		
		MstdApp app;
		MstdApi api;

		public LoginMstd(Callback origin) {
			this.origin = origin;
		}

		@Override
		public void onFinish() {
			
			msMain(origin.from(),origin,true);
			
			super.onFinish();
			
		}
		
	}

	@Override
	public void onCallback(UserData user,Callback callback,String point,String[] params) {
	
		if (POINT_LOGIN.equals(point)) {
			
			LoginMstd login = (LoginMstd) setPrivatePoint(user,POINT_LOGIN,new LoginMstd(callback));
			
			callback.edit("请输入实例域名 :)").withCancel().exec(login)
		
			return;
			
		}
		
	}
	
	
	@Override
	public void onPoint(UserData user,Msg msg,String point,PointData data) {

		LoginMstd login = (LoginMstd) data;

		if (login.step == 0) {
			
			if (!msg.hasText() || !msg.text().contains(".")) {
				
				msg.send("这不是一个 Mastodon 实例的域名，你知道吗？你刚刚点了认证 Mastodon 账号，但是你没有点取消就在发送其他内容 :(","每次给你这种人写这种提示很麻烦的，理解一下好吗？").exec(login);
				
				return;
				
			}
			
			MstdApp app;
			
			MstdApi api = new MstdApi(msg.text());

			if (MstdApp.data.containsId(msg.text())) {

				app = MstdApp.data.getById(msg.text());

			} else {

				AppRegistration reg;

				try {

					reg = api.apps().createApp("NTT","urn:ietf:wg:oauth:2.0:oob",new Scope(Scope.Name.ALL),"https://manual.kurumi.io").execute();

				} catch (Mastodon4jRequestException e) {

					msg.send("实例登记失败 : " + e.getMessage()).async();

					return;

				}

				app = new MstdApp();

				app.id = msg.text();
				app.appId = reg.getId();
				app.clientId = reg.getClientId();
				app.clientSecret = reg.getClientSecret();

				app.data.setById(app.id,app);

			}

			String url = api.apps().getOAuthUrl(app.clientId,new Scope(Scope.Name.ALL),"urn:ietf:wg:oauth:2.0:oob");

			login.app = app;
			login.api = api;
			
			login.step = 1;

			msg.send("戳这里验证 : " + Html.a("戳这里",url),"发送得到的验证码给咱就可以了 :)").withCancel().html().exec(login);
			
			return;
			
		}
		
		MstdApp app = login.app;
		MstdApi api = login.api;

		try {

			AccessToken token = api.apps().getAccessToken(app.clientId,app.clientSecret,msg.text()).execute();

			clearPrivatePoint(user);

			MstdAuth auth = new MstdAuth();

			auth.id = user.id;
			auth.appId = app.id;
			auth.accessToken = token.getAccessToken();

			Account account = auth.createApi().accounts().getVerifyCredentials().execute();

			MstdAuth.data.setById(auth.id,auth);

			msg.send("好，认证成功 : " + account.getDisplayName() + " (@" + account.getUserName() + "@" + app.id + ") .").async();

		} catch (Mastodon4jRequestException e) {

			msg.send("认证失败 : " + e.getMessage()).async();

		}
		
	}

	
}
