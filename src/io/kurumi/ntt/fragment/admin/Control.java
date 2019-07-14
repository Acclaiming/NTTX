package io.kurumi.ntt.fragment.admin;

import cn.hutool.core.util.RuntimeUtil;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.BotLog;
import io.kurumi.ntt.Launcher;

public class Control extends Fragment {

		@Override
		public void init(BotFragment origin) {

				super.init(origin);

				registerAdminFunction("stop","upgrade","restart","poweroff","reboot");

    }

    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params) {

         if ("upgrade".equals(function)) {

						new Send(Env.GROUP,"Bot Update Executed : By " + user.userName()).html().exec();

						// Launcher.INSTANCE.stop();

            try {

								String str = RuntimeUtil.execForStr("bash update.sh");

								new Send(Env.GROUP,"update successful , now restarting...\n",str).exec();
								
						} catch (Exception e) {

								new Send(Env.GROUP,"update failed",BotLog.parseError(e)).exec();

						}
						
						RuntimeUtil.exec("service ntt restart");
						
				} else if ("restart".equals(function)) {

            new Send(Env.GROUP,"Bot Restart Executed : By " + user.userName()).html().exec();

						Launcher.INSTANCE.stop();

            RuntimeUtil.exec("service ntt restart");

        } else if ("poweroff".equals(function)) {

						Launcher.INSTANCE.stop();

            new Send(Env.GROUP,"Server Stop Executed : By " + user.userName()).html().exec();

            RuntimeUtil.exec("poweroff");

        } else if ("reboot".equals(function)) {

						Launcher.INSTANCE.stop();

            new Send(Env.GROUP,"Bot Restart Executed : By " + user.userName()).html().exec();

            RuntimeUtil.exec("reboot");

        } 

    }

}
