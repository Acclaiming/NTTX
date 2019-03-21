package io.kurumi.ntt.luaj;

import io.kurumi.ntt.db.*;
import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.model.*;
import java.util.*;
import org.luaj.vm2.*;
import org.luaj.vm2.compiler.*;
import io.kurumi.ntt.model.request.*;

public class LuaDaemon {

	public Globals luaj;

	private Fragment fragment;
	private UserData user;
	
	public LuaDaemon(BotFragment fragment,UserData user) {

		this.fragment = fragment;
		
		this.user = user;
		
		luaj = new Globals();

		LuaC.install(luaj);
		
		luaj.load(new PrintFunc());
		
	}

	public LuaValue exec(String lua) {
		
		return luaj.load(lua).call();

	}
	
	public class PrintFunc extends LuaFunction {

		@Override
		public String name() {
			
			return "print";
			
		}
		
		@Override
		public LuaValue call() {
			
			new Send(fragment,"print").exec();
			
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
