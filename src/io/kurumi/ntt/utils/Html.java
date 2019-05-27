package io.kurumi.ntt.utils;

import cn.hutool.http.*;

public class Html {
	
	public static String a(String text,String href) {
		
		return "<a href=\"" + href + "\">" + HtmlUtil.escape(text) + "</a>";
		
	}
	
}
