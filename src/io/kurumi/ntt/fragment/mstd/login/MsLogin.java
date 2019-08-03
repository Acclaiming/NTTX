package io.kurumi.ntt.fragment.mstd.login;

import cn.hutool.core.util.StrUtil;
import com.sys1yagi.mastodon4j.api.Scope;
import com.sys1yagi.mastodon4j.api.entity.Account;
import com.sys1yagi.mastodon4j.api.entity.auth.AccessToken;
import com.sys1yagi.mastodon4j.api.entity.auth.AppRegistration;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import io.kurumi.ntt.db.PointData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.mstd.MstdApi;
import io.kurumi.ntt.fragment.mstd.MstdApp;
import io.kurumi.ntt.fragment.mstd.MstdAuth;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.Html;

public class MsLogin extends Fragment {

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

		if (MstdAuth.data.containsId(user.id)) {
			
			msg.send("已经认证过账号了唔","使用 /ms_logout 移除 :)").async();
			
			return;
			
		}
		
		if (params.length == 1) {

			LoginMstd login = (MsLogin.LoginMstd) setPrivatePoint(user,POINT_MSTD_LOGIN,new LoginMstd());

			MstdApp app;

			MstdApi api = new MstdApi(params[0]);

			if (MstdApp.data.containsId(params[0])) {

				app = MstdApp.data.getById(params[0]);

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

			msg.send("戳这里验证 : " + Html.a("戳这里",url),"发送得到的验证码给咱就可以了 :)").withCancel().html().exec(login);

		} else if (params.length == 3) {

			MstdApp app;

			MstdApi api = new MstdApi(params[0]);

			if (MstdApp.data.containsId(params[0])) {

				app = MstdApp.data.getById(params[0]);

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

			try {

				AccessToken token = api.apps().postUserNameAndPassword(app.clientId,app.clientSecret,new Scope(Scope.Name.ALL),params[1],params[2]).execute();

				msg.delete();

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

		} else {
			
			msg.send("/ms_login <域名>").async();
			
		}

	}

	@Override
	public void onPoint(UserData user,Msg msg,String point,PointData data) {

		LoginMstd login = (LoginMstd) data;

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
