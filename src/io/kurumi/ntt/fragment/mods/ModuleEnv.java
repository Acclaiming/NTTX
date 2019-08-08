package io.kurumi.ntt.fragment.mods;

import io.kurumi.ntt.Env;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import cn.hutool.core.io.FileUtil;
import com.google.gson.Gson;
import io.kurumi.ntt.Launcher;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import java.io.IOException;
import io.kurumi.ntt.utils.BotLog;
import java.util.concurrent.atomic.AtomicLongArray;

public class ModuleEnv {

	public static HashMap<Long,ModuleEnv> envs = new HashMap<>();

	public static File mainPath = new File(Env.DATA_DIR,"mods/users");

	public static void loadAllEnv() {

	}
	
	public static ModuleEnv get(Long userId) {
		
		if (exiting.containsKey(userId)) return null;
		
		if (envs.containsKey(userId)) {
			
			return envs.get(userId);
			
		}
		
		ModuleEnv mod = new ModuleEnv(userId);

		envs.put(userId,mod);
		
		return mod;
		
	}
	
	public static HashMap<Long,Boolean> exiting = new HashMap<>();
	
	public static void exitEnv(Long userId) {
		
		exiting.put(userId,true);
		
		envs.remove(userId);
		
		ModuleEnv toExit = envs.get(userId);

		if (toExit != null) toExit.env.ctx.exit();
		
	}

	public long userId;
	public File path;

	public JavaScriptEnv env;

	public Scriptable functions;

	public ModuleEnv(long userId) {

		this.userId = userId;
		this.path = new File(mainPath,userId + "");

		env = new JavaScriptEnv();

		functions = env.ctx.newObject(env.env);

		env.putConst("__F",functions);
		
		reLoadModules();

	}

	public HashMap<String,NModule> modules = new HashMap<>();

	public HashMap<String,NModule> functionIndex = new HashMap<>();

	public void reLoadModules() {

		File[] files = path.listFiles();

		if (files == null || files.length == 0) return;
		
		for (File modDir : path.listFiles()) {
			
			NModule mod = Launcher.GSON.fromJson(FileUtil.readUtf8String(new File(modDir,"package.json")),NModule.class);

			mod.modPath = modDir;
			
			modules.put(mod.id,mod);

		}

		for (NModule mod : modules.values()) {

			for (String dep : mod.dependencies) {
				
				if (!modules.containsKey(dep)) {
					
					mod.error = new ModuleException("模块 " + mod.id + " 缺少依赖 " + dep);
					
					continue;
					
				}
				
			}
			
			for (String fn : mod.cmds) {

				NModule err = functionIndex.put(fn,mod);

				if (err != null) {

					mod.error = new ModuleException("模块函数重复 " + fn + " 在 : " + mod.id + " 与 " + err.id);

					continue;
					
				}

			}
			
			File mainPath = new File(mod.modPath,mod.main);
			
			if (!mainPath.exists()) {
				
				mod.error = new ModuleException("入口文件 " + mod.main + " 不存在");
				
				continue;
				
			}
			
			mod.env = env.ctx.newObject(env.env);
			
			ScriptableObject.putConstProperty(mod.env,"mod",mod);
			
			try {
				
				env.ctx.evaluateReader(mod.env,FileUtil.getUtf8Reader(mainPath),mainPath.getName(),1,null);
				
			} catch (Exception e) {
				
				mod.error = new ModuleException("初始化出错 : \n\n" + BotLog.parseError(e));
				
			}

		}



	}

}
