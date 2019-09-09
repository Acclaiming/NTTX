package io.kurumi.ntt.utils;

import cn.hutool.core.util.StrUtil;

import java.math.BigInteger;

/**
 * *
 * *    零宽度字符水印
 * *
 * *    https://www.freebuf.com/articles/web/167903.html
 * *
 **/

public class ZeroPad {

    public static String decodeFrom(String text) {

        String encoded = "";

        for (char c : text.toCharArray()) {

            if (c == 8203) {

                encoded += "0";

            } else if (c == 8204) {

                encoded += "1";

            }

        }

        if (encoded.length() == 0) return "";

        if (encoded.startsWith("0")) {

            encoded = encoded.substring(1);

        } else {

            encoded = "-" + encoded.substring(1);

        }

        return StrUtil.utf8Str(new BigInteger(encoded, 2).toByteArray());

    }

    static String[] stringSplit(String text, int length) {

        if (length > text.length()) length = text.length();

        String[] array = new String[length];

        int split = text.length() / length;

        int index = 0;

        for (; index < array.length; index++) {

            array[index] = text.substring(index * split, (index + 1) * split);

        }

        int last = text.length() % length;

        if (split != 0) {

            array[index - 1] = array[index - 1] + text.substring((index) * split, ((index) * split) + last);

        }

        return array;

    }

    public static String encodeTo(String text, String content) {

        text = text.replace((char) 8203 + "", "").replace((char) 8204 + "", "");

        String encoded = new BigInteger(StrUtil.utf8Bytes(content)).toString(2);

        if (!encoded.startsWith("-")) {

            encoded = "0" + encoded;

        } else {

            encoded = "1" + encoded.substring(1);

        }

        content = "";

        for (char str : encoded.toCharArray()) {

            if (str == '0') {

                content += (char) 8203;

            } else {

                content += (char) 8204;

            }

        }

        if (text.length() < 2) {

            return content + text;

        }

        String[] encodedArray = stringSplit(content, text.length() - 1);

        StringBuilder result = new StringBuilder();

        int index = 0;

        for (; index < encodedArray.length; index++) {

            result.append(text.substring(index, index + 1));

            result.append(encodedArray[index]);

        }

        if (index < text.length()) {

            result.append(text.substring(index, text.length()));

        }

        return result.toString();

    }

}
