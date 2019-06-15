package io.kurumi.ntt.fragment.admin;

import cn.hutool.core.util.RuntimeUtil;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.abs.Function;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.fragment.abs.request.Send;

import java.util.LinkedList;
import io.kurumi.ntt.utils.*;

public class Control extends Function {

    @Override
    public void functions(LinkedList<String> names) {

        names.add("stop");
		names.add("upgrade");
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

        }  else if ("upgrade".equals(function)) {

			new Send(Env.GROUP, "Bot Update Executed : By " + user.userName()).html().exec();

            try {

				String str = RuntimeUtil.execForStr("bash /usr/local/ntt/update.sh");

				new Send(Env.GROUP, "update successful , now restarting...\n",str).exec();
				
				RuntimeUtil.exec("service ntt restart");
				
			} catch (Exception e) {
				
				new Send(Env.GROUP,"update failed",BotLog.parseError(e)).exec();
				
			}
			
			
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
