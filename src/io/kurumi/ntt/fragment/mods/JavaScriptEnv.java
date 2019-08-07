package io.kurumi.ntt.fragment.mods;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class JavaScriptEnv {
	
	public Context ctx;
	public Scriptable env;
	
	public JavaScriptEnv() {
		
		ctx = Context.enter();
		ctx.setOptimizationLevel(-1);
		ctx.setLanguageVersion(Context.VERSION_ES6);
		
		ImporterTopLevel importer = new ImporterTopLevel();
		importer.initStandardObjects(ctx,false);
		env = importer;
		
	}
	
	public JavaScriptEnv(Context ctx,Scriptable env) {
		
		this.ctx = Context.enter();
		this.ctx.setOptimizationLevel(-1);
		this.ctx.setLanguageVersion(Context.VERSION_ES6);
		
		this.env = ctx.newObject(env);
		this.env.setParentScope(null);
		
	}
	
	public void putVar(String name,Object obj) {
		
		ScriptableObject.putProperty(env,name,obj);
		
	}
	
	public void putConst(String name,Object obj) {

		ScriptableObject.putConstProperty(env,name,obj);

	}
	
}
