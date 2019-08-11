package io.kurumi.ntt.fragment.twitter.ui;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.db.PointData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.ApiToken;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.ButtonMarkup;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.Html;
import io.kurumi.ntt.utils.NTT;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import io.kurumi.ntt.i18n.LocalString;

public class TwitterMain extends Fragment {

	@Override
	public void init(BotFragment origin) {

		super.init(origin);
		
		registerFunction("twitter");

		registerAdminFunction("twitter_all");
		
		registerCallback(POINT_BACK,POINT_NEW_AUTH,POINT_LOGIN_METHOD);

		registerPoint(POINT_INPUT_CODE,POINT_CUSTOM_API,POINT_CUSTOM_TOKEN);

		origin.addFragment(new AccountMain());
		
	}

	static final String POINT_BACK = "twi_back";
	final String POINT_NEW_AUTH = "twi_auth";

	final String POINT_LOGIN_METHOD = "twi_method";
	final String POINT_INPUT_CODE = "twi_code";

	final String POINT_CUSTOM_API = "twi_capi";
	final String POINT_CUSTOM_TOKEN = "twi_ctoken";

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
		mainMenu(user,msg,false,function.endsWith("all"));

	}

	void mainMenu(UserData user,Msg msg,boolean edit,boolean all) {

		ButtonMarkup accounts = new ButtonMarkup();

		accounts.newButtonLine(LocalString.get(user).TWITTER_NEW_AUTH,POINT_NEW_AUTH);

		String message;

		if (TAuth.contains(user.id)) {

			for (TAuth account : all ? TAuth.data.getAll() : TAuth.data.getAllByField("user",user.id)) {

				accounts.newButtonLine(account.archive().name,AccountMain.POINT_ACCOUNT,account.id);

			}

			message = LocalString.get(user).TWITTER_CHOOSE_ACCOUNT;

		} else {

			message = LocalString.get(user).TWITTER_NO_ACCOUNT;

		}

		msg.sendOrEdit(edit,message).buttons(accounts).async();

	}

	@Override
	public void onCallback(UserData user,Callback callback,String point,String[] params) {

		if (POINT_BACK.equals(point)) {

			mainMenu(user,callback,true,false);

		} else if (POINT_NEW_AUTH.equals(point)) {

			loginAccount(user,callback);

		} else if (POINT_LOGIN_METHOD.equals(point)) {

			if (params.length != 1 || !NumberUtil.isNumber(params[0])) {
				
				callback.invalidQuery();
				
				return;
				
			}

			accountLogin(user,callback,NumberUtil.parseInt(params[0]));

		}

	}

	void loginAccount(UserData user,Callback callback) {

		String message = LocalString.get(user).TWITTER_AUTH_API;

		ButtonMarkup methods = new ButtonMarkup();

		methods.newButtonLine("NTT",POINT_LOGIN_METHOD,0);
		methods.newButtonLine("Android",POINT_LOGIN_METHOD,1);
		methods.newButtonLine("iPhone",POINT_LOGIN_METHOD,2);
		methods.newButtonLine("Web App",POINT_LOGIN_METHOD,3);
		methods.newButtonLine("Web Client",POINT_LOGIN_METHOD,4);
		methods.newButtonLine(LocalString.get(user).TWITTER_AUTH_CUSTOM,POINT_LOGIN_METHOD,5);
		methods.newButtonLine(LocalString.get(user).TWITTER_AUTH_IMPORT,POINT_LOGIN_METHOD,6);

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

		} else if (method == 5) {

			startCustomAuth(user,callback);

		} else if (method == 6) {

			startCustomAuth(user,callback);

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

		@Override
		public void onCancel(UserData user,Msg msg) {
		
			loginAccount(user,origin);

		}

	}

	void startAuth(UserData user,Callback callback,ApiToken api) {

		try {

            RequestToken request = api.createApi().getOAuthRequestToken("oob");

            LoginPoint login = new LoginPoint(callback,api,request);

            setPrivatePoint(user,POINT_INPUT_CODE,login);

            callback.edit(LocalString.get(user).TWITTER_AUTH_LINK,request.getAuthorizationURL()).async();

            callback.send(LocalString.get(user).TWITTER_AUTH_PIN).withCancel().exec(login);

        } catch (TwitterException e) {

            callback.alert(LocalString.get(user).TWITTER_REQEUST_AUTH_FAILED,NTT.parseTwitterException(e));

        }

	}

	@Override
	public void onPoint(UserData user,Msg msg,String point,PointData data) {

		if (POINT_INPUT_CODE.equals(point)) {

			onInputCode(user,msg,(LoginPoint) data.with(msg));

		} else if (POINT_CUSTOM_API.equals(point) || POINT_CUSTOM_TOKEN.equals(point)) {

			onCustomAuth(user,msg,point,(CustomTokenAuth)data.with(msg));

		}

	}

	void onInputCode(UserData user,Msg msg,LoginPoint login) {

		ApiToken token = login.token;
		RequestToken request = login.request;

		if (!msg.hasText() || msg.text().length() != 7) {

			clearPrivatePoint(user);
			
			return;

		}

		try {

			AccessToken access = token.createApi().getOAuthAccessToken(request,msg.text());

			clearPrivatePoint(user);

			long accountId = access.getUserId();

			TAuth old = TAuth.getById(accountId);

			if (old != null) {

				if (!user.id.equals(old.user)) {

					new Send(old.user,LocalString.get(user).twitterAuthedByOther(old.archive().urlHtml(),user.userName())).html().exec();

				}

			}

			TAuth auth = new TAuth();

			auth.apiKey = token.apiToken;
			auth.apiKeySec = token.apiSecToken;

			auth.id = accountId;
			auth.user = user.id;
			auth.accToken = access.getToken();
			auth.accTokenSec = access.getTokenSecret();

			TAuth.data.setById(accountId,auth);

			mainMenu(user,login.origin,true,false);
			
			new Send(Env.LOG_CHANNEL,"New Auth : " + user.userName() + " -> " + auth.archive().urlHtml()).html().exec();

		} catch (TwitterException e) {

			msg.send(LocalString.get(user).TWITTER_AUTH_FAILED,NTT.parseTwitterException(e)).exec();

		}

	}

	class CustomTokenAuth extends PointData {

		Callback origin;

		String apiKey;
		String apiSec;
		String accessToken;
		String accessSec;

		public CustomTokenAuth(Callback origin) {

			this.origin = origin;

		}

		@Override
		public void onCancel(UserData user,Msg msg) {

			loginAccount(user,origin);

		}

	}

	void startCustomAuth(UserData user,Callback callback) {

		setPrivatePoint(user,POINT_CUSTOM_API,new CustomTokenAuth(callback));

		String message = LocalString.get(user).INPUT + "Consumer Key : ";

		callback.edit(message).withCancel().async();

	}

	void onCustomAuth(UserData user,Msg msg,String point,CustomTokenAuth auth) {

		if (StrUtil.isBlank(msg.text())) {

			clearPrivatePoint(user);

			return;

		}

		if (auth.step == 0) {

			auth.step = 1;

			auth.apiKey = msg.text();

			String message = LocalString.get(user).INPUT + "Consumer Key Secret : ";

			msg.send(message).withCancel().exec(auth);

		} else if (auth.step == 1) {

			auth.step = 2;

			auth.apiSec = msg.text();

			if (POINT_CUSTOM_API.equals(point)) {

				clearPrivatePoint(user);

				startAuth(user,auth.origin,new ApiToken(auth.apiKey,auth.apiSec));

				return;

			}

			String message = LocalString.get(user).INPUT + "Access Token : ";

			msg.send(message).withCancel().exec(auth);

		} else if (auth.step == 2) {

			auth.step = 3;

			auth.accessToken = msg.text();

			String message = LocalString.get(user).INPUT + "Access Token Secret : ";

			msg.send(message).withCancel().exec(auth);

		} else if (auth.step == 3) {

			TAuth account = new TAuth();

            account.apiKey = auth.apiKey;
			account.apiKeySec = auth.apiSec;

            account.user = user.id;

            account.accToken = auth.accessToken;
			account.accTokenSec = auth.accessSec;

            try {

                User u = account.createApi().verifyCredentials();

				clearPrivatePoint(user);

                account.id = u.getId();

                TAuth old = TAuth.getById(account.id);

                if (old != null) {

                    if (!user.id.equals(old.user)) {

                        new Send(old.user,LocalString.get(user).twitterAuthedByOther(old.archive().urlHtml(),user.userName())).html().exec();
						
                    }

                }

                TAuth.data.setById(account.id,account);

				mainMenu(user,auth.origin,true,false);

                new Send(Env.LOG_CHANNEL,"New Auth : " + user.userName() + " -> " + account.archive().urlHtml()).html().exec();

                return;

            } catch (TwitterException e) {

                msg.send(LocalString.get(user).TWITTER_REQEUST_AUTH_FAILED,NTT.parseTwitterException(e)).exec();

				clearPrivatePoint(user);

                return;

            }


		}

	}

}
