package io.kurumi.ntt.luaj;

import org.luaj.vm2.*;
import io.kurumi.ntt.db.*;
import java.util.*;

public class LuaDaemon {

	public Globals luaj;

	public LuaDaemon() {

		luaj = new Globals();

	}

	public LuaValue exec(String lua) {

		return luaj.load(lua).call();

	}

	private static HashMap<UserData,LuaDaemon> cache = new HashMap<>();

	public static LuaDaemon get(UserData user) {

		if (cache.containsKey(user)) return cache.get(user);

		LuaDaemon daemon = new LuaDaemon();

		cache.put(user,daemon);

		return daemon;

	}

}
