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
import io.kurumi.ntt.Env;

public class TwitterUI extends Fragment {

	public static TwitterUI INSTANCE = new TwitterUI();

	@Override
	public boolean onMsg(UserData user,Msg msg) {

		if (!msg.isCommand()) return false;

		switch (msg.command()) {

			case "login" : tauth(user,msg);break;

			case "logout" : trem(user,msg);break;

			default : return false;

		}

		return true;

	}

	@Override
	public boolean onPPM(UserData user,Msg msg) {
		
		switch (user.point.getPoint()) {

			case POINT_INPUT_CALLBACK : onInputCallback(user,msg);break;

			default : return false;

		}

		return true;

	}

	final String POINT_INPUT_CALLBACK = "t|i";

	HashMap<UserData,RequestToken> cache = new HashMap<>();

	void tauth(UserData user,Msg msg) {

        if (TAuth.exists(user.id)) {

            msg.send("乃已经认证账号了 :)").publicFailed();

            return;

		}

        if (!msg.isPrivate()) {

            msg.send("请使用私聊 :)").publicFailed();

            return;

        }

		msg.send("好的。现在咱正在向Twitter请求认证连接 :D").exec();

		try {

			RequestToken request = ApiToken.defaultToken.createApi().getOAuthRequestToken("oob");

			cache.put(user,request);

			msg.send("请求成功 :) 点 [这里](" + request.getAuthorizationURL() + ") 认证 ~").markdown().exec();

			// msg.send("因为咱是一个简单的程序 所以不能自动收到认证！ T_T ","","请记住 : 认证账号之后会跳转到一个不可访问的界面 : 在浏览器显示的地址是 127.0.0.1 , 这时候不要关闭浏览器！复制这个链接并发送给咱就可以了 ~","","如果不小心关闭了浏览器 请使用 /cancel 取消认证并重新请求认证 ^_^").exec();

            msg.send("输入PIN码即可 (灬ºωº灬)").exec();

			user.point = cdata(POINT_INPUT_CALLBACK);

			// 不需要保存Point 因为request token的cache也不会保存。

		} catch (TwitterException e) {

			msg.send(e.toString()).exec();

			msg.send("请求认证链接失败 :( ","这可能是因为同时请求的人太多，或者有人不停重复请求... 也有可能是咱Twitter账号被停用了。 ","","那么，请再来一次吧 ~").exec();

		}

	}

	void onInputCallback(UserData user,Msg msg) {

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


        RequestToken requestToken = cache.get(user);

		if (requestToken == null) {

			msg.send("缓存丢失 请使用 /cancel 并重新认证 :(").exec();

		} else {

			try {


				AccessToken access = ApiToken.defaultToken.createApi().getOAuthAccessToken(requestToken,msg.text());

				TAuth auth = new TAuth(ApiToken.defaultToken.apiToken,ApiToken.defaultToken.apiSecToken,access.getToken(),access.getTokenSecret());

                user.point = null;

                if (!auth.refresh()) {

                    msg.send("认证错误... 请重试").exec();

                    return;

                }

                synchronized (TAuth.auth) {

                    TAuth.auth.put(user.id.toString(),auth);

                }

                TAuth.saveAll();

				msg.send("好！现在认证成功 , " + auth.getFormatedNameHtml()).html().exec();
                
                GroupProtecter.userAuthed(user.id);

                new Send(this,Env.DEVELOPER_ID,user.userName() + " 认证了 " + auth.getFormatedNameHtml()).html().exec();

			} catch (TwitterException e) {

				msg.send("链接好像失效了...","请重新认证 /login (｡>∀<｡)").exec();

			}

        }

	}

	void trem(UserData user,Msg msg) {

		if (!TAuth.exists(user.id))  {

			msg.send("乃并没有认证账号呢。 使用 /login 认证 ~").publicFailed();

			return;

		}

        synchronized (TAuth.auth) {

            TAuth.auth.remove(user.id.toString());

        }

        TAuth.saveAll();

		msg.send("好！现在认证已经移除 ~ 程序不会继续保存。 [关于Twitter认证部分的代码](https://github.com/HiedaNaKan/NTTools/tree/master/src/io/kurumi/ntt/funcs/TwitterUI.java)。").markdown().exec();

	}

}
