package io.kurumi.ntt.fragment.tieba;

import com.github.libsgh.tieba.api.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.funcs.abs.*;
import io.kurumi.ntt.model.*;
import java.io.*;
import java.util.*;
import cn.hutool.http.*;
import com.pengrad.telegrambot.request.*;

public class TiebaLogin extends Function {

	@Override
	public void functions(LinkedList<String> names) {

		names.add("tblogin");

	}

	final String POINT_TIEBA_LOGIN = "tieba,login";

	@Override
	public void points(LinkedList<String> points) {
	
		points.add(POINT_TIEBA_LOGIN);
	
	}
	
    static class TiebaLoginStatus {

		int status = 0;

		String userName;

		String password;

		String imgUrl;
		String codestring;
		String cookies;
		String token;

		// 0 输入账号
		// 1 输入密码
		// 2 验证码

	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		setPoint(user,POINT_TIEBA_LOGIN,new TiebaLoginStatus());

		msg.send("输入用户名 :").exec();

	}

	@Override
	public void onPoint(UserData user,Msg msg,PointStore.Point point) {

		TiebaLoginStatus status = (TiebaLoginStatus)point.data;

		if (status.status == 0) {

			if (!msg.hasText()) {

				msg.send("正在登录贴吧 : 请输入用户名","使用 /cancel 取消").exec();

				return;

			}

			status.status = 1;

			status.userName = msg.text();

			msg.send("现在输入密码 :").exec();

		} else if (status.status == 1) {

			if (!msg.hasText()) {

				msg.send("正在登录贴吧 : 请输入密码","使用 /cancel 取消").exec();

				return;

			}

			status.password = msg.text();

			msg.sendTyping();

			Map<String, Object> result = TieBaApi.getInstance().getBaiDuLoginCookie(status.userName,status.password,null,null,null,null);

			processResult(user,msg,status,result);
			
		} else if (status.status == 2) {

			Map<String, Object> result = TieBaApi.getInstance().getBaiDuLoginCookie(status.userName,status.password,msg.text(),status.codestring,status.cookies,status.token);

			processResult(user,msg,status,result);
			
		}

	}

	void processResult(UserData user,Msg msg,TiebaLogin.TiebaLoginStatus status,Map<String, Object> result) {

		msg.sendTyping();
		
		if (result.get("status") == null) {
			
			msg.send("网络问题，请重试....").exec();
			
		} else if (result.get("status").toString().equals("0")) {

			clearPoint(user);

			loginSuccess(user,msg,status,result);

		} else if (result.get("status").toString().equals("-1")) {

			status.imgUrl = result.get("imgUrl").toString();
			status.codestring = result.get("codestring").toString();
			status.cookies = result.get("cookies").toString();
			status.token = result.get("token").toString();

			msg.sendUpdatingPhoto();

			File img = new File(Env.CACHE_DIR,"tieba_code/" + status.codestring);

			HttpUtil.downloadFile(status.imgUrl,img);

			bot().execute(new SendPhoto(msg.chatId(),img).caption(result.get("message").toString() + "\n\n验证码图片URL : " + status.imgUrl));

			status.status = 2;

		} else {

			msg.send(result.get("message").toString()).exec();

			if (!result.get("status").equals("-3")) {

				clearPoint(user);

			}

		}


	}

	void loginSuccess(UserData user,Msg msg,TiebaLogin.TiebaLoginStatus status,Map<String, Object> result) {

		String bduss = result.get("bduss").toString();
		String ptoken = result.get("ptoken").toString();
		String stoken = result.get("stoken").toString();

		BAuth auth = new BAuth();

		auth.id = user.id;
		auth.bduss = bduss;
		auth.ptoken = ptoken;
		auth.stoken = stoken;

		BAuth.data.setById(user.id,auth);

		Map<String, Object> userInfo = TieBaApi.getInstance().getUserInfo(bduss,stoken);

		msg.send(userInfo.toString()).exec();

	}



}
