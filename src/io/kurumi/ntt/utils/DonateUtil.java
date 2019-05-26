package io.kurumi.ntt.utils;

import cn.hutool.core.util.*;
import cn.hutool.http.*;
import cn.hutool.json.*;

public class DonateUtil {
	
	public static boolean ccLogin(String email,String password) {

		HttpUtil.get("https://app.cloudcone.com/login");

		HttpResponse resp = HttpUtil.createPost("https://app.cloudcone.com/ajax/visitor")
			.form("method","login")
			.form("email",email)
			.form("password",password)
			.form("_token","null")
			.execute();

		return new JSONObject(resp.body()).getInt("status") == 1;
	}

	public static String ccAlipay(int amount) {

		String html = HttpUtil.get("https://app.cloudcone.com/billing");

		String token = StrUtil.subBetween(html,"_token\" value=\"","\">");

		HttpResponse resp = HttpUtil.createPost("https://app.cloudcone.com/ajax/payments")
			.form("amount",amount)
			.form("method","funds.addbyalipay")
			.form("_token",token)
			.execute();

		return new JSONObject(resp.body()).getByPath("__data.redirect",String.class);

	}

	public static String ccPaypal() {

		String html = HttpUtil.get("https://app.cloudcone.com/billing");
	
		StringBuilder ret = new StringBuilder("<html>");
		
		ret.append("<head><title>Redirecting...</title></head>");
		
		ret.append("<body><form id=\"donate\" action=\"https://www.paypal.com/cgi-bin/webscr");
		
		ret.append(StrUtil.subBetween(html,"https://www.paypal.com/cgi-bin/webscr","</form>"));
		
		ret.append("</form><script>document.getElementById('donate').submit()</script></body></html>");
		
		return ret.toString();
		
	}
	
}
