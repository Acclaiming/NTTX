package io.kurumi.ntt.fragment.twitter;

import cn.hutool.core.util.NumberUtil;
import io.kurumi.ntt.db.PointData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.ButtonMarkup;
import io.kurumi.ntt.utils.Html;
import io.kurumi.ntt.utils.NTT;
import twitter4j.TwitterException;
import twitter4j.auth.RequestToken;
import java.util.HashMap;
import twitter4j.auth.AccessToken;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.Env;

public class TwitterMain extends Fragment {

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerFunction("twitter");

	}

	final String POINT_BACK = "twi_back";
	final String POINT_NEW_AUTH = "twi_auth";
	final String POINT_ACCOUNT = "twi_show_account";

	final String POINT_LOGIN_METHOD = "twi_method";
	final String POINT_INPUT_CODE = "twi_code";

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		mainMenu(user,msg,false);

	}

	void mainMenu(UserData user,Msg msg,boolean edit) {

		ButtonMarkup accounts = new ButtonMarkup();

		accounts.newButtonLine("认证新账号",POINT_NEW_AUTH);

		String message;

		if (TAuth.contains(user.id)) {

			for (TAuth account : TAuth.data.getAllByField("user",user.id)) {

				accounts.newButtonLine(account.archive().name,POINT_ACCOUNT,account.id);

			}

			message = "选择一个账号查看设置 或者认证你的账号 :)";

		} else {

			message = "还没有认证账号 :)";

		}

		msg.sendOrEdit(edit,message).buttons(accounts).async();

	}

	@Override
	public void onCallback(UserData user,Callback callback,String point,String[] params) {

		if (POINT_BACK.equals(point)) {

			mainMenu(user,callback,true);

		} else if (POINT_NEW_AUTH.equals(point)) {

			loginAccount(user,callback);

		} else if (POINT_LOGIN_METHOD.equals(point)) {

			if (params.length != 1 || !NumberUtil.isNumber(params[0])) return;

			accountLogin(user,callback,NumberUtil.parseInt(params[0]));

		}

	}

	void loginAccount(UserData user,Callback callback) {

		String message = "请选择接口 (发送的推文下方显示的来源)";

		ButtonMarkup methods = new ButtonMarkup();

		methods.newButtonLine("NTT",POINT_LOGIN_METHOD,0);
		methods.newButtonLine("Android",POINT_LOGIN_METHOD,1);
		methods.newButtonLine("iPhone",POINT_LOGIN_METHOD,2);
		methods.newButtonLine("Web App",POINT_LOGIN_METHOD,3);
		methods.newButtonLine("Web Client",POINT_LOGIN_METHOD,4);
		methods.newButtonLine("第三方接口",POINT_LOGIN_METHOD,5);
		methods.newButtonLine("恢复认证",POINT_LOGIN_METHOD,6);

		methods.newButtonLine("🔙",POINT_BACK);

		callback.edit(message).buttons(methods).async();

	}

	void accountLogin(UserData user,Callback callback,int method) {

		if (method == 0) {

			startAuth(user,callback,ApiToken.defaultToken);

		} else if (method == 1) {

			startAuth(user,callback,ApiToken.androidToken);

		} else if (method == 2) {

			startAuth(user,callback,ApiToken.iPhoneToken);

		} else if (method == 3) {

			startAuth(user,callback,ApiToken.webAppToken);
			
		} else if (method == 4) {

			startAuth(user,callback,ApiToken.webClientToken);

		}

	}

	class LoginPoint extends PointData {

		Callback origin;

		ApiToken token;
		RequestToken request;

		public LoginPoint(Callback origin,ApiToken token,RequestToken request) {

			this.origin = origin;
			this.token = token;
			this.request = request;

		}

	}

	void startAuth(UserData user,Callback callback,ApiToken api) {

		try {

            RequestToken request = api.createApi().getOAuthRequestToken("oob");

            LoginPoint login = new LoginPoint(callback,api,request);

            setPrivatePoint(user,POINT_INPUT_CODE,login.with(callback));

            callback.edit("点 " + Html.a("这里",request.getAuthorizationURL()) + " 认证 :)").html().async();

            callback.send("(｡•̀ᴗ-)✧ 请输入 pin 码 : ").withCancel().exec(login);

        } catch (TwitterException e) {

            callback.alert("请求认证链接失败 :( ",NTT.parseTwitterException(e));

        }

	}

	@Override
	public void onPoint(UserData user,Msg msg,String point,PointData data) {
		
		if (POINT_INPUT_CODE.equals(point)) {
			
			inputCode(user,msg,(LoginPoint) data.with(msg));
			
		}
		
	}
	
	void inputCode(UserData user,Msg msg,LoginPoint login) {
		
		ApiToken token = login.token;
		RequestToken request = login.request;
		Callback origin = login.origin;

		if (!msg.hasText() || msg.text().length() != 7) {
			
			msg.send("当前正在登录 Twitter 账号，请输入 PIN 码").withCancel().exec(login);
			
			return;
			
		}
		
		try {

			AccessToken access = token.createApi().getOAuthAccessToken(request, msg.text());

			clearPrivatePoint(user);
			
			long accountId = access.getUserId();

			TAuth old = TAuth.getById(accountId);

			if (old != null) {

				if (!user.id.equals(old.user)) {

					new Send(old.user, "乃的账号 " + old.archive().urlHtml() + " 已被 " + user.userName() + " 认证 , 已移除。").html().exec();

				}

			}

			TAuth auth = new TAuth();

			auth.apiKey = token.apiToken;
			auth.apiKeySec = token.apiSecToken;

			auth.id = accountId;
			auth.user = user.id;
			auth.accToken = access.getToken();
			auth.accTokenSec = access.getTokenSecret();

			TAuth.data.setById(accountId, auth);
			
			mainMenu(user,origin,true);

			new Send(Env.LOG_CHANNEL, "New Auth : " + user.userName() + " -> " + auth.archive().urlHtml()).html().exec();

		} catch (TwitterException e) {

			msg.send("认证失败...", NTT.parseTwitterException(e)).exec();

		}
		
		
	}

}
