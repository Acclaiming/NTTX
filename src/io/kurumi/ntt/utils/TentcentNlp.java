package io.kurumi.ntt.utils;

import cn.hutool.json.JSONObject;
import cn.xsshome.taip.nlp.TAipNlp;

public class TentcentNlp {

    static final String APP_ID = "2111414508";
    static final String APP_KEY = "e1Z3XyOYI7VuVQP0";

    static TAipNlp nlp = new TAipNlp(APP_ID,APP_KEY);

    public static float nlpTextpolar(String text) {

        try {


            JSONObject data = new JSONObject(nlp.nlpTextpolar(text));

            if (data.getInt("ret") != 0) {

                BotLog.debug(data.toStringPretty());

                return 0;

            }

            return data.getFloat("polar") * data.getFloat("confd");

        } catch (Exception e) {}

        return 0;

    }

    public static String nlpTextchat(String id,String text) {

        try {

            text = text.replace("🐴","妈");

            JSONObject data = new JSONObject(nlp.nlpTextchat(id.toString(),text));

            if (data.getInt("ret") != 0) {

                BotLog.debug(data.toStringPretty());

                return null;

            }

            String reply = data.getByPath("data.answer",String.class);

            return reply
                .replace("先让我堵上耳朵，捂上眼睛","噫呜呜噫 ~")
                .replace("好好说话","好好当个rbq")
                .replace("不明白你说的什么意思","呜呜呜")
                .replace("听不大懂耶","「 数据删除 」")
                .replace("今天的新闻","女装援交")
                .replace("妈","🐴");

        } catch (Exception e) {}

        return null;

    }


}
