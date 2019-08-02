package io.kurumi.ntt.fragment.mstd.login;

import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.Scope;
import com.sys1yagi.mastodon4j.api.method.Apps;
import io.kurumi.ntt.Launcher;
import io.kurumi.ntt.db.PointData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.fragment.mstd.MstdApp;
import cn.hutool.core.util.StrUtil;
import com.sys1yagi.mastodon4j.MastodonRequest;
import com.sys1yagi.mastodon4j.api.entity.auth.AppRegistration;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.entity.auth.AccessToken;
import io.kurumi.ntt.fragment.mstd.MstdAuth;
import com.sys1yagi.mastodon4j.api.entity.Account;
import com.sys1yagi.mastodon4j.api.method.Accounts;
import io.kurumi.ntt.fragment.mstd.MstdApi;
import io.kurumi.ntt.fragment.BotFragment;

public class MstdLogin extends Fragment {

	final String POINT_MSTD_LOGIN = "mstd_login";

	class LoginMstd extends PointData {
		
		MstdApp app;
		MstdApi api;
		
	}

	@Override
	public void init(BotFragment origin) {
	
		super.init(origin);
		
		registerFunction("ms_login");
		
		registerPoint(POINT_MSTD_LOGIN);
		
	}
	
	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		setPrivatePoint(user,POINT_MSTD_LOGIN,new LoginMstd());

		msg.send("输入 Mastodon 实例域名 :").async();

	}

	@Override
	public void onPoint(UserData user,Msg msg,String point,PointData data) {

		LoginMstd login = (LoginMstd) data;
		
		if (data.step == 0) {

			if (StrUtil.isBlank(msg.text())) {

				msg.send("请输入 Mastodon 实例域名").withCancel().async();

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
			
			login.step = 1;
			login.app = app;
			login.api = api;
			
			msg.send("打开链接认证并发送得到的验证码 : " + url).withCancel().async();
			
		} else {
			
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

}
