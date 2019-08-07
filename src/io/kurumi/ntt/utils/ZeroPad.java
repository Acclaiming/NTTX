package io.kurumi.ntt.utils;

import cn.hutool.core.util.StrUtil;
import java.math.BigInteger;

public class ZeroPad {

	public static String decodeFrom(String text) {

		String encoded;

		if (text.charAt(0) == 8203) {

			encoded = "";

		} else if (text.charAt(0) == 8204) {

			encoded = "-";

		} else {

			return "";

		}

		for (char c : text.substring(1).toCharArray()) {

			if (c == 8203) {

				encoded += "0";

			} else if (c == 8204) {

				encoded += "1";

			} else break;

		}

		return StrUtil.utf8Str(new BigInteger(encoded,2).toByteArray());

	}

	public static String encodeTo(String text,String content) {

		char current;

		while (text.length() > 0 && ((current = text.charAt(0)) == 8203 || current == 8204)) text = text.substring(1);

		String encoded = new BigInteger(StrUtil.utf8Bytes(content)) .toString(2);

		if (!encoded.startsWith("-")) {
			
			encoded = "0" + encoded;
			
		} else {
			
			encoded = "1" + encoded.substring(1);
			
		}

		content = "";

		for (char str : encoded.toCharArray()) {

			if (str == '0') {

				content += (char)8203;

			} else {

				content += (char)8204;

			}

		}

		return content + text;

	}

}
