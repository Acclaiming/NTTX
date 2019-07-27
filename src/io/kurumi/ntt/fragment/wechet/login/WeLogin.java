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

public class WeLogin extends Fragment {

	final String POIMT_LOGIN = "we_login";

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerFunction("we_login");

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

		executeAsync(msg.update,new SendPhoto(msg.chatId(),bytes).caption("请扫码登录 有效期 1 - 7 天 (玄学)").replyMarkup(button.markup()));

	}



}
