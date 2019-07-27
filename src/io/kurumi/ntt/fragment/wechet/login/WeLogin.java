package io.kurumi.ntt.fragment.wechet.login;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.fragment.wechet.WeAuth;
import me.xuxiaoxiao.chatapi.wechat.WeChatApi;
import java.util.HashMap;
import me.xuxiaoxiao.chatapi.wechat.protocol.RspLogin;
import cn.hutool.http.HttpUtil;
import com.pengrad.telegrambot.request.SendPhoto;
import io.kurumi.ntt.model.request.ButtonMarkup;
import io.kurumi.ntt.model.Callback;
import java.net.HttpCookie;
import me.xuxiaoxiao.xtools.common.http.XHttpTools;
import com.google.gson.Gson;

public class WeLogin extends Fragment {

	final String POIMT_LOGIN = "we_login";

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerFunction("we_login");

		registerCallback(POIMT_LOGIN);
		
	}

	HashMap<Long,WeChatApi> loginCache = new HashMap<>();

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		if (WeAuth.data.containsId(user.id)) {

			msg.send("你已经登录，使用 /we_logout 登出。").exec();

			return;

		}

		WeChatApi api = new WeChatApi();

		String img = api.jslogin();

		if (img == null) {

			msg.send("获取登录验证码失败，请重试。").async();

			return;

		}

		byte[] bytes = HttpUtil.createGet(img).execute().bodyBytes();

		loginCache.put(user.id,api);

		ButtonMarkup button = new ButtonMarkup();

		button.newButtonLine("已经扫码",POIMT_LOGIN);

		execute(new SendPhoto(msg.chatId(),bytes).caption("请扫码登录 有效期 1 - 7 天 (玄学)").replyMarkup(button.markup()));

	}

	@Override
	public void onCallback(UserData user,Callback callback,String point,String[] params) {
		
		if (!loginCache.containsKey(user.id)) {
			
			callback.alert("登录二维码失效，请重新登陆。");
			
			callback.delete();
			
			return;
			
		}
		
		WeChatApi api = loginCache.get(user.id);

		RspLogin loginResult = api.login();

		if (loginResult.code == 200) {
			
			api.webwxnewloginpage(loginResult.redirectUri);
			
			WeAuth auth = new WeAuth();
			
			auth.id = user.id;
			
			auth.host = api.host;
			auth.uin = api.uin;
			auth.sid = api.sid;
			auth.skey = api.skey;
			auth.passticket = api.passticket;
			
			for (HttpCookie cookie : api.httpExecutor.getCookies()) {
				
				if ("wxsid".equalsIgnoreCase(cookie.getName())) {
					
					auth.sid = cookie.getValue();
					
				} else if ("wxuin".equalsIgnoreCase(cookie.getName())) {
					
					auth.uin = cookie.getValue();
					
				} else if ("webwx_data_ticket".equalsIgnoreCase(cookie.getName())) {
					
					auth.dataTicket = cookie.getValue();
					
				}
				
			}
			
			WeAuth.data.setById(user.id,auth);
			
			callback.send(new Gson().toJson(auth)).exec();
			
		} else if (loginResult.code == 201 || loginResult.code == 408) {
			
			callback.alert("请扫描二维码 :)");
			
		} else {
			
			callback.alert("扫描二维码超时，请重新登录。");
			
			callback.delete();
			
		}
		
	}

}
