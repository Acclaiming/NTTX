package io.kurumi.ntt.fragment.admin;

import cn.hutool.core.util.RuntimeUtil;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.abs.Function;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.fragment.abs.request.Send;

import java.util.LinkedList;

public class Control extends Function {

    @Override
    public void functions(LinkedList<String> names) {

        names.add("stop");
        names.add("restart");
        names.add("poweroff");
        names.add("reboot");
        names.add("rdate");

    }

    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params) {

        if (!user.developer()) {

            msg.send("Permission Denied").exec();

            return;

        }

        if ("stop".equals(function)) {

            new Send(Env.GROUP, "Bot Stop Executed : By " + user.userName()).html().exec();

            RuntimeUtil.exec("service ntt stop");

        } else if ("restart".equals(function)) {

            new Send(Env.GROUP, "Bot Restart Executed : By " + user.userName()).html().exec();

            RuntimeUtil.exec("service ntt restart");

        } else if ("poweroff".equals(function)) {

            new Send(Env.GROUP, "Server Stop Executed : By " + user.userName()).html().exec();

            RuntimeUtil.exec("poweroff");

        } else if ("reboot".equals(function)) {

            new Send(Env.GROUP, "Bot Restart Executed : By " + user.userName()).html().exec();

            RuntimeUtil.exec("reboot");

        } else if ("rdate".equals(function)) {

            new Send(Env.GROUP, "Time Sync Executed : By " + user.userName()).html().exec();

            RuntimeUtil.execForStr("rdate -s time.nist.gov");

        }

    }

}
