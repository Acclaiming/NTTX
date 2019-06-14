package io.kurumi.ntt.fragment;

import io.kurumi.ntt.Env;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.abs.Function;
import io.kurumi.ntt.fragment.abs.Msg;

import java.util.LinkedList;

public class Donate extends Function {

    @Override
    public void functions(LinkedList<String> names) {

        names.add("donate");
        names.add("ccinit");

    }

    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params) {

        if ("donate".equals(function)) {

            msg.send("感谢。点击下方Url为服务器账户 (位于CloudCone) 氪金。", "由于支付宝官方限制 氪金金额至少为 $5 氪金请谨慎 🤣 (", "戳这里 : https://" + Env.get("server_domain") + "/donate?amount=5").exec();

        } else {

            if (!user.developer()) {

                msg.send("permission denied").exec();

                return;

            }

            if (params.length != 2) {

                msg.send("/ccinit <email> <password>").exec();

                return;

            }

            Env.set("donate.cc.email", params[0]);
            Env.set("donate.cc.password", params[1]);

            msg.send("successful").exec();

        }

    }

}
