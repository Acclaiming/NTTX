package io.kurumi.ntt.fragment.mods;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.BotFragment;
import cn.hutool.core.util.StrUtil;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;
import cn.hutool.core.util.ArrayUtil;

public class PackageManager extends Fragment {

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerFunction("yum");

	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		String[] mainHelp = new String[] {

			"用法  : /yum <命令> [参数...]",

			"\n/yum install <packages>",
			"/yum show <packages>",
			"/yum reinstall <packages>",
			"/yum uninstall <packages>",
			"/yum search <query>",
			"/yum update",
			"/yum list-local",
			"/yum list-all",

		};
		
		if (params.length == 0) {
			
			msg.send(mainHelp).async();
			
			return;
			
		}
		
		String subFn = params[0];

		params = ArrayUtil.remove(params,0);
		
		//if ("install".equals()) {}
		
	}

}
