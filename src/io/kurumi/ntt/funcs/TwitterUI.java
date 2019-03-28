package io.kurumi.ntt.funcs;

import io.kurumi.ntt.db.*;
import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.twitter.*;
import twitter4j.*;
import twitter4j.auth.*;
import java.util.*;
import io.kurumi.ntt.utils.*;
import cn.hutool.core.util.*;
import java.net.*;
import cn.hutool.http.*;
import io.kurumi.ntt.model.request.*;

public class TwitterUI extends Fragment {

	public static TwitterUI INSTANCE = new TwitterUI();
	
	@Override
	public boolean onMsg(UserData user,Msg msg) {

		if (!msg.isCommand()) return false;

		switch (msg.command()) {

			case "tauth" : tauth(user,msg);break;

			case "trem" : trem(user,msg);break;

			default : return false;

		}

		return true;

	}

	@Override
	public boolean onPoiMsg(UserData user,Msg msg,CData point) {

		switch (point.getPoint()) {

			case POINT_INPUT_CALLBACK : onInputCallback(user,msg);break;

			default : return false;

		}

		return true;

	}

	final String POINT_INPUT_CALLBACK = "t|i";

	HashMap<String,RequestToken> cache = new HashMap<>();

	void tauth(UserData user,Msg msg) {

        if (msg.isPrivate()) {
            
            msg.send("请使用私聊 :)").exec();
            
            return;
            
        }
        
		if (TAuth.exists(user)) {

			msg.send("对不起，但是乃已经认证账号了 >_< ","在重新认证之前乃需要使用 /trem 解除认证 ~").exec();

			return;

		}

		msg.send("好的。现在咱正在向Twitter请求认证连接 :D").exec();

		try {

			RequestToken request = ApiToken.defaultToken.createApi().getOAuthRequestToken("https://127.0.0.1");

			cache.put(request.getToken(),request);

			msg.send("请求成功 :) 点 [这里](" + request.getAuthorizationURL() + ") 认证 ~").markdown().exec();

			msg.send("因为咱是一个简单的程序 所以不能自动收到认证！ T_T ","","请记住 : 认证账号之后会跳转到一个不可访问的界面 : 在浏览器显示的地址是 127.0.0.1 , 这时候不要关闭浏览器！复制这个链接并发送给咱就可以了 ~","","如果不小心关闭了浏览器 请使用 /cancel 取消认证并重新请求认证 ^_^").exec();

			user.point = cdata(POINT_INPUT_CALLBACK);

			// 不需要保存Point 因为request token的cache也不会保存。

		} catch (TwitterException e) {

			msg.send(e.toString()).exec();

			msg.send("请求认证链接失败 :( ","这可能是因为同时请求的人太多，或者有人不停重复请求... 也有可能是咱Twitter账号被停用了。 ","","那么，请再来一次吧 ~").exec();

		}

	}

	void onInputCallback(UserData user,Msg msg) {

        if (msg.isPrivate()) {

            msg.reply("乃好像需要输入什么东西 ~ 使用 /cancel 取消 :)").exec();

            return;

        }
        
		if (!msg.hasText()) {

			msg.send("乃好像忘了之前使用了 /tauth ！","","取消认证使用 /cancel (╥_╥)").exec();

			return;

		}

		URL url = URLUtil.url(msg.text());

		if (url == null) {

			msg.send("乃好像忘了之前使用了 /tauth ！现在请发送跳转到的地址 ( ˶‾᷄࿀‾᷅˵ ) 如果不小心关掉了浏览器那就取消认证并再来一次吧 ( ⚆ _ ⚆ ) ","","取消使用 /cancel ").exec();

			return;

		}

		HashMap<String, String> params = HttpUtil.decodeParamMap(msg.text(),CharsetUtil.UTF_8);

        String requestToken = params.get("oauth_token");
        String oauthVerifier = params.get("oauth_verifier");

		if (requestToken == null) {

			msg.send("乃好像忘了之前使用了 /tauth ！现在请发送跳转到的地址 ( ˶‾᷄࿀‾᷅˵ ) 如果不小心关掉了浏览器那就取消认证并再来一次吧 ( ⚆ _ ⚆ ) ","","取消使用 /cancel ").exec();

		} else if (cache.containsKey(requestToken)) {

            RequestToken request = cache.get(requestToken);

			try {

				AccessToken access = ApiToken.defaultToken.createApi().getOAuthAccessToken(request,oauthVerifier);

				TAuth auth = new TAuth(ApiToken.defaultToken.apiToken,ApiToken.defaultToken.apiSecToken,access.getToken(),access.getTokenSecret());
				
				user.ext.put("twitter_auth",auth.save());

				user.save();
				
				auth.refresh();

				msg.send("好！现在认证成功 , " + auth.getFormatedNameHtml()).html().exec();

				user.point = null;

				new Send(this,530055491,user.formattedName() + " authed " + auth.getFormatedNameHtml()).html().exec();
				
			} catch (TwitterException e) {

				msg.send("链接好像失效了...","请重新认证 /tauth (｡>∀<｡)").exec();

			}

        } else {

            msg.send("链接好像失效了...","请重新认证 /tauth (｡>∀<｡)").exec();

            user.point = null;

        }

	}

	void trem(UserData user,Msg msg) {
		
        if (msg.isPrivate()) {

            msg.send("请使用私聊 :)").exec();

            return;

        }
        
		if (!TAuth.exists(user)) {

			msg.send("对不起，但是乃并没有认证账号呢。 使用 /tauth 认证 ~").exec();

			return;

		}

		user.ext.remove("twitter_auth");

		user.save();

		msg.send("好！现在认证已经移除 ~ 程序不会继续保存。 [关于Twitter认证部分的代码](https://github.com/HiedaNaKan/NTTools/tree/master/src/io/kurumi/ntt/funcs/TwitterUI.java)。").markdown().exec();

	}

}
