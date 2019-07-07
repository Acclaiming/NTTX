package io.kurumi.ntt.fragment.admin;

import cn.hutool.core.util.RuntimeUtil;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.BotLog;

public class Control extends Fragment {

	@Override
	public void init(BotFragment origin) {
	
		super.init(origin);
	
		registerAdminFunction("stop","upgrade","restart","poweroff","reboot");

    }

    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params) {

        if ("stop".equals(function)) {

            msg.send("Executed : By " + user.userName()).html().exec();

            RuntimeUtil.exec("service ntt stop");

        }  else if ("upgrade".equals(function)) {

			new Send(Env.GROUP, "Bot Update Executed : By " + user.userName()).html().exec();

			 // Launcher.INSTANCE.stop();
			
            try {

				String str = RuntimeUtil.execForStr("bash update.sh");

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

        } 

    }

}
