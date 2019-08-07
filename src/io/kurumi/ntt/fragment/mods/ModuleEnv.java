package io.kurumi.ntt.fragment.mods;

import io.kurumi.ntt.Env;
import java.io.File;
import java.util.HashMap;
import java.util.List;

public class ModuleEnv {
	
	public static HashMap<Long,ModuleEnv> envs = new HashMap<>();

	public static File mainPath = new File(Env.DATA_DIR,"mods");
	
	public static void loadAllEnv() {
		
	}
	
	public long userId;
	public File path;
	
	public ModuleEnv(long userId) {

		this.userId = userId;
		this.path = new File(mainPath,userId + "");
		
	}
	
	public List<NModule> module;
	
	public void loadModules() {
		
		
		
	}
	
}
