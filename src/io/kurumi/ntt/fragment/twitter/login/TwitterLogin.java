package io.kurumi.ntt.fragment.twitter.login;

import cn.hutool.core.util.NumberUtil;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.ApiToken;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.Html;
import io.kurumi.ntt.utils.NTT;
import java.util.HashMap;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.not;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;
import static java.util.Arrays.asList;

public class TwitterLogin extends Fragment {

    final String POINT_INPUT_CALLBACK = "twitter_login";
    public HashMap<Long, RequestToken> cache = new HashMap<>();

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerFunction("login");

		registerPoints(POINT_INPUT_CALLBACK);

    }

	@Override
	public int checkFunction() {

		return FUNCTION_PRIVATE;

	}

    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params) {

		ApiToken token;

		if (params.length == 2 || params.length == 4) {

			token = new ApiToken(params[0],params[1]);

		} else if (params.length == 1) {
			
			if ("android".equals(params[0].toLowerCase())) {
				
				token = ApiToken.androidToken;
				
			} else if ("iphone".equals(params[0].toLowerCase())) {
				
				token = ApiToken.iPhoneToken;
			
			} else if ("web".equals(params[0].toLowerCase())) {
				
				token = ApiToken.webToken;
				
			} else {
				
				msg.send("/login","or /login [web|android|iphone]","or /login <apiKey> <apiKeySec>","or /login <apiKey> <apiKeySec> <accToken> <accTokenSec>").exec();
				
				return;
				
			}
			
		} else if (params.length > 0) {

			msg.send("/login","or /login [web|android|iphone]","or /login <apiKey> <apiKeySec>","or /login <apiKey> <apiKeySec> <accToken> <accTokenSec>").exec();

			return;

		} else {

			token = ApiToken.defaultToken;

		}

		if (params.length == 4) {

			TAuth auth = new TAuth();

			auth.apiKey = params[0];
			auth.apiKeySec = params[1];

			auth.user = user.id;

			auth.accToken = params[2];
			auth.accTokenSec = params[3];

			try {

				User account = auth.createApi().verifyCredentials();

				auth.id = account.getId();

				TAuth old = TAuth.getById(auth.id);

                if (old != null) {

                    if (!user.id.equals(old.user)) {

                        new Send(old.user,"乃的账号 " + old.archive().urlHtml() + " 已被 " + user.userName() + " 认证（●＾o＾●").html().exec();

                    }

                }

				if (account.getAccessLevel() < 2) {
					
					msg.send("注意！这个API只读！NTT的某些功能不可用。").exec();
					
				}
				
                TAuth.data.setById(auth.id,auth);

                msg.send("好！现在认证成功 , " + auth.archive().urlHtml()).html().exec();

                cache.remove(user.id);

                new Send(Env.GROUP,"New Auth : " + user.userName() + " -> " + auth.archive().urlHtml()).html().exec();

				return;

			} catch (TwitterException e) {

				msg.send("检查认证失败",NTT.parseTwitterException(e)).exec();

				return;

			}

		}

        try {

            RequestToken request = token.createApi().getOAuthRequestToken("oob");

            cache.put(user.id,request);

            msg.send("点 " + Html.a("这里",request.getAuthorizationURL()) + " 认证 ( 支持多账号的呢 ~").html().exec();

            // msg.send("因为咱是一个简单的程序 所以不能自动收到认证！ T_T ","","请记住 : 认证账号之后会跳转到一个不可访问的界面 : 在浏览器显示的地址是 127.0.0.1 , 这时候不要关闭浏览器！复制这个链接并发送给咱就可以了 ~","","如果不小心关闭了浏览器 请使用 /cancel 取消认证并重新请求认证 ^_^").exec();

            msg.send("(｡•̀ᴗ-)✧ 请输入 pin 码 : ","使用 /cancel 取消 ~").exec();

            setPrivatePointData(user,POINT_INPUT_CALLBACK,token);

            // 不需要保存Point 因为request token的cache也不会保存。

        } catch (TwitterException e) {

            msg.send("请求认证链接失败 :( ",NTT.parseTwitterException(e)).exec();

        }


    }

	@Override
	public void onPoint(UserData user,Msg msg,String point,Object data) {

        if (!msg.hasText() || msg.text().length() != 7 || !NumberUtil.isNumber(msg.text())) {

            msg.reply("乃好像需要输入认证的 7位 PIN码 ~ 使用 /cancel 取消 :)").exec();

            return;

        }

        /*

         URL url = URLUtil.url(msg.text());

         if (url == null) {

         msg.send("乃好像忘了之前使用了 /login ！现在请发送跳转到的地址 ( ˶‾᷄࿀‾᷅˵ ) 如果不小心关掉了浏览器那就取消认证并再来一次吧 ( ⚆ _ ⚆ ) ","","取消使用 /cancel ").exec();

         return;

         }

         HashMap<String, String> params = HttpUtil.decodeParamMap(msg.text(),CharsetUtil.UTF_8);

         */


        RequestToken requestToken = cache.get(user.id);

        if (requestToken == null) {

            clearPrivatePoint(user);

            msg.send("缓存丢失 请重新认证 :(").exec();

        } else {

            try {

				ApiToken token = (ApiToken) data;
				
                AccessToken access = token.createApi().getOAuthAccessToken(requestToken,msg.text());

                long accountId = access.getUserId();

                TAuth old = TAuth.getById(accountId);

                if (old != null) {

                    if (!user.id.equals(old.user)) {

                        new Send(old.user,"乃的账号 " + old.archive().urlHtml() + " 已被 " + user.userName() + " 认证（●＾o＾●").html().exec();

                    }

                }

                TAuth auth = new TAuth();

                auth.apiKey = token.apiToken;
                auth.apiKeySec = token.apiSecToken;

                auth.id = accountId;
                auth.user = user.id;
                auth.accToken = access.getToken();
                auth.accTokenSec = access.getTokenSecret();

                clearPrivatePoint(user);

				if (auth.createApi().verifyCredentials().getAccessLevel() < 2) {

					msg.send("注意！这个API只读！NTT的某些功能不可用。").exec();

				}
				
                TAuth.data.setById(accountId,auth);

                msg.send("好！现在认证成功 , " + auth.archive().urlHtml()).html().exec();

                cache.remove(user.id);

                new Send(Env.GROUP,"New Auth : " + user.userName() + " -> " + auth.archive().urlHtml()).html().exec();

            } catch (TwitterException e) {

                msg.send("链接好像失效了...",NTT.parseTwitterException(e)).exec();

            }


        }
    }


}
