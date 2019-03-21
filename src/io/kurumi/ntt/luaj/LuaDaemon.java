package io.kurumi.ntt.luaj;

import io.kurumi.ntt.db.*;
import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.model.request.*;
import java.util.*;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.*;
import org.luaj.vm2.luajc.*;

public class LuaDaemon {

	public Globals luaj;

	private Fragment fragment;
	private UserData user;
	
	public LuaDaemon(BotFragment fragment,UserData user) {

		this.fragment = fragment;
		
		this.user = user;
		
		luaj = JsePlatform.standardGlobals();

		LuaJC.install(luaj);
		
	}

	public LuaValue exec(String lua) {
		
		return luaj.load(lua).call();

	}
	
	public class PrintFunc extends VarArgFunction {

		@Override
		public Varargs invoke(Varargs p1) {
			// TODO: Id
			return super.invoke(p1);
		}
		
		@Override
		public LuaValue call(LuaValue msg) {
			
			new Send(fragment,msg.toString()).exec();
			
			return NIL;
			
		}
		
	}

	private static HashMap<UserData,LuaDaemon> cache = new HashMap<>();

	public static LuaDaemon get(BotFragment fragment,UserData user) {

		if (cache.containsKey(user)) return cache.get(user);

		LuaDaemon daemon = new LuaDaemon(fragment,user);

		cache.put(user,daemon);

		return daemon;

	}

}
