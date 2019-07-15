package io.kurumi.ntt.utils;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.json.JSONObject;
import io.kurumi.ntt.Launcher;
import io.kurumi.ntt.fragment.Fragment;

public class Html {

		public static String escape(String text) {
				
				return HtmlUtil.escape(text)
						.replace(" ","&nbsp;")
						.replace("¡","&iexcl;")
						.replace("¢","&cent;")
						.replace("£","&pound;")
						.replace("¤","&curren;")
						.replace("¥","&yen;")
						.replace("¦","&brvbar;")
						.replace("§","&sect;")
						.replace("¨","&uml;")
						.replace("©","&copy;")
						.replace("ª","&ordf;")
						.replace("«","&laquo;")
						.replace("¬","&not;")
						.replace("soft","&shy;")
						.replace("®","&reg;")
						.replace("¯","&macr;")
						.replace("°","&deg;")
						.replace("±","&plusmn;")
						.replace("²","&sup2;")
						.replace("³","&sup3;")
						.replace("´","&acute;")
						.replace("µ","&micro;")
						.replace("¶","&para;")
						.replace("·","&middot;")
						.replace("¸","&cedil;")
						.replace("¹","&sup1;")
						.replace("º","&ordm;")
						.replace("»","&raquo;")
						.replace("¼","&frac14;")
						.replace("½","&frac12;")
						.replace("¾","&frac34;")
						.replace("¿","&iquest;")
						.replace("×","&times;")
						.replace("÷","&divide;");
				
		}
		
		public static String unescape(String text) {
				
				return HtmlUtil.unescape(text)
						.replace("&nbsp;"," ")
						.replace("&iexcl;","¡")
						.replace("&cent;","¢")
						.replace("&pound;","£")
						.replace("&curren;","¤")
						.replace("&yen;","¥")
						.replace("&brvbar;","¦")
						.replace("&sect;","§")
						.replace("&uml;","¨")
						.replace("&copy;","©")
						.replace("&ordf;","ª")
						.replace("&laquo;","«")
						.replace("&not;","¬")
						.replace("&shy;","soft")
						.replace("&reg;","®")
						.replace("&macr;","¯")
						.replace("&deg;","°")
						.replace("&plusmn;","±")
						.replace("&sup2;","²")
						.replace("&sup3;","³")
						.replace("&acute;","´")
						.replace("&micro;","µ")
						.replace("&para;","¶")
						.replace("&middot;","·")
						.replace("&cedil;","¸")
						.replace("&sup1;","¹")
						.replace("&ordm;","º")
						.replace("&raquo;","»")
						.replace("&frac14;","¼")
						.replace("&frac12;","½")
						.replace("&frac34;","¾")
						.replace("&iquest;","¿")
						.replace("&times;","×")
						.replace("&divide;","÷");
				
		}
		
		public static String b(Object text) {

        return "<b>" + escape(text.toString()) + "</b>";

    }

		public static String i(Object text) {

        return "<i>" + escape(text.toString()) + "</i>";

    }


    public static String a(String text,String href) {

        return "<a href=\"" + href + "\">" + escape(text) + "</a>";

    }

    public static String user(String text,long id) {

        if (text.isEmpty()) {

            text = " ";

        }

        return a(text,"tg://user?id=" + id);

    }

		public static String twitterUser(String text,String id) {

        if (text.isEmpty()) {

            text = " ";

        }

        return a(text,"https://twitter.com/" + id);

    }

    public static String startPayload(String text,Object... payload) {

        return startPayload(Launcher.INSTANCE,text,payload);

    }

		public static String startPayload(Fragment fragment,String text,Object... payload) {

        return a(text,"https://t.me/" + fragment.origin.me.username() + "?start=" + ArrayUtil.join(payload,"_"));

    }

		public static String json(Object code) {

        return code(new JSONObject(code).toStringPretty());

    }


    public static String code(Object code) {

        return "<code>" + escape(code.toString()) + "</code>";

    }

}
