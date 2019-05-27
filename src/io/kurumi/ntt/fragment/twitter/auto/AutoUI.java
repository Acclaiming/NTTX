package io.kurumi.ntt.fragment.twitter.auto;

import io.kurumi.ntt.funcs.abs.*;
import java.util.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.*;

public class AutoUI extends TwitterFunction {

	public class AutoSetting {

		public Long id;

		public boolean like = false;

	}

	public static Data<AutoSetting> autoData = new Data<AutoSetting>(AutoSetting.class);

	@Override
	public void functions(LinkedList<String> names) {

		names.add("auto");

	}


	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {

		boolean enable;

		if (params.length < 2 || !(enable = "enable".equals(params[0]) || "disable".equals(params[0]))) {

			msg.send("/auto [enable|disable] <action>").exec();

			return;

		}

		AutoSetting setting = autoData.getById(account.id);

		if (setting == null) setting = new AutoSetting();

		for (String field : params[1].split(" ")) {

			switch (field) {

				case "like" : setting.like = enable;break;

			}

		}

		autoData.setById(setting.id,setting);
		
		msg.send("完成。").exec();

	}


}
