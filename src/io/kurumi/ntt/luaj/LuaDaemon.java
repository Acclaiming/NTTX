package io.kurumi.ntt.luaj;

import io.kurumi.ntt.db.*;
import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.model.request.*;
import java.util.*;
import org.luaj.vm2.*;
import org.luaj.vm2.compiler.*;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.*;

public class LuaDaemon {

	public Globals luaj;

	private Fragment fragment;
	private UserData user;
	
	public LuaDaemon(BotFragment fragment,UserData user) {

		this.fragment = fragment;
		
		this.user = user;
		
		luaj = JsePlatform.standardGlobals();
		
		luaj.get("_G").set("print",new print());

	}

	public LuaValue exec(String lua) {
		
		return luaj.load(lua).call();

	}

	private static HashMap<UserData,LuaDaemon> cache = new HashMap<>();

	public static LuaDaemon get(BotFragment fragment,UserData user) {

		if (cache.containsKey(user)) return cache.get(user);

		LuaDaemon daemon = new LuaDaemon(fragment,user);

		cache.put(user,daemon);

		return daemon;

	}
	
	public class print extends VarArgFunction {

		@Override
		public Varargs invoke(Varargs args) {
			
			StringBuilder msg = new StringBuilder();
			
			for (int index = 0;index < args.narg();index ++) {
				
				msg.append(args.arg(index + 1));
				
			}
			
			new Send(fragment,user.id,msg.toString()).exec();
			
			return NIL;
			
		}
		
	}

}
