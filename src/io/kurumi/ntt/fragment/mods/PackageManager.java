package io.kurumi.ntt.fragment.mods;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.BotFragment;
import cn.hutool.core.util.StrUtil;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;
import cn.hutool.core.util.ArrayUtil;
import io.kurumi.ntt.Env;
import java.io.File;
import cn.hutool.core.util.RuntimeUtil;
import io.kurumi.ntt.Launcher;
import java.util.LinkedList;
import cn.hutool.core.io.FileUtil;
import java.util.List;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.HttpResponse;
import io.kurumi.ntt.utils.BotLog;

public class PackageManager extends Fragment {

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerFunction("yum");

	}

	File envPath = new File(Env.DATA_DIR,"mods/env");

	String executeGitCommand(File path,String... command) {

		path.mkdirs();
		
		return RuntimeUtil.getResult(RuntimeUtil.exec(null,path,command));

	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		String[] mainHelp = new String[] {

			"用法  : /yum <命令> [参数...]",

			"\n/yum install <package>",
			// "/yum show <package>",
			// "/yum reinstall <package>",
			// "/yum uninstall <package>",
			// "/yum search <query>",
			// "/yum update",
			// "/yum list-local",
			// "/yum list-all",

		};

		if (params.length == 0) {

			msg.send(mainHelp).async();

			return;

		}

		Msg status = msg.send("正在更新模块源...").send();

		if (!new File(envPath,".git").isDirectory()) {

			RuntimeUtil.execForStr("git clone " + Env.MODULES_REPO + "/packages " + envPath.getPath());

		} else {

			executeGitCommand(envPath,"git pull");

			// executeGitCommand("git fetch --depth=1 origin master && git checkout -f FETCH_HEAD && git clean -fdx");

		}

		File packagesFile = new File(envPath,"packages.json");

		List<String> mods = (List<String>) Launcher.GSON.fromJson(FileUtil.readUtf8String(packagesFile),List.class);

		String subFn = params[0];

		params = ArrayUtil.remove(params,0);

		if ("install".equals(subFn)) {

			if (params.length == 0) {

				msg.send(mainHelp).async();

				return;

			}

			String modName = params[0];

			ModuleEnv env = ModuleEnv.get(user.id);

			if (env.modules.containsKey(modName)) {

				NModule mod = env.modules.get(modName);

				if (!mods.contains(modName)) {

					status.edit("模块仓库无该记录 本地仓库无需更新").async();

					return;

				}
				
				HttpResponse result = HttpUtil.createGet(Env.formatRawFile(modName,"package.json")).execute();

				if (!result.isOk()) {

					status.edit(mod.name + " 检查更新失败 : " + result.getStatus() + "\n\n" + result.body()).async();

					return;

				}

				NModule syncMod;

				try {

					syncMod = Launcher.GSON.fromJson(result.body(),NModule.class);

				} catch (Exception ex) {

					status.edit("检查更新失败 : 模块设定格式错误",BotLog.parseError(ex)).async();

					return;

				}

				if (syncMod.versionCode <= mod.versionCode) {

					status.edit("模块 " + mod.name + " 已经是最新版本").async();

					return;

				}

				status.edit("正在更新模块 : " + mod.name + " [ " + mod.version + " --> " + syncMod.version + " ] ").async();
				
				ModuleEnv.exitEnv(user.id);
				
				executeGitCommand(mod.modPath,"git fetch --depth=1 origin master && git checkout -f FETCH_HEAD && git clean -fdx");
				
				status.edit("模块已更新 : " + mod.name + " [ " + mod.version + " --> " + syncMod.version + " ] ").async();
				
				ModuleEnv.exiting.remove(user.id);
				
				return;
				
			}
			
			if (!mods.contains(modName)) {

				status.edit("模块源没有该模块 : " + modName).async();

				return;

			}

			HttpResponse result = HttpUtil.createGet(Env.formatRawFile(modName,"package.json")).execute();

			if (!result.isOk()) {

				status.edit(modName + " 元数据获取失败 : " + result.getStatus() + "\n\n" + result.body()).async();

				return;

			}
			
			NModule mod;

			try {

				mod = Launcher.GSON.fromJson(result.body(),NModule.class);

				mod.modPath = new File(env.mainPath,mod.name);
				
			} catch (Exception ex) {

				status.edit("元数据获取失败 : 模块设定格式错误",BotLog.parseError(ex)).async();

				return;

			}
			
			status.edit("正在安装模块 : " + mod.name + " [ " + mod.version + " ] ").async();

			ModuleEnv.exitEnv(user.id);

			executeGitCommand(env.mainPath,"git clone " + Env.MODULES_REPO + "/" + modName);

			status.edit("模块已安装 : " + mod.name + " [ " + mod.version + " ] ").async();

			ModuleEnv.exiting.remove(user.id);
			
		}

	}

}
