package io.kurumi.ntt.funcs;

import io.kurumi.ntt.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.model.request.*;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.*;
import java.io.*;
import cn.hutool.core.io.*;
import cn.hutool.core.util.*;
import io.kurumi.ntt.utils.*;
import org.luaj.vm2.lib.*;
import java.util.*;

public class LuaEnv extends Fragment {

	public static LuaEnv INSTANCE = new LuaEnv();

	public LuaTable env;
	public LuaTable functions;

	public LuaFunction require;

	File funcDir = new File("./lua");

	public Globals lua; { reset();

		File[] funcs = funcDir.listFiles();

		for (File func : funcs) {

			if (func.getName().endsWith(".lua")) {

				try {

					lua.loadfile(func.getPath()).call();

				} catch (LuaError err) {

					err.printStackTrace();

				}


			}

		}

	}


	void reset() {

		lua = JsePlatform.standardGlobals();

		env = lua.get("_G").checktable();

		functions = new LuaTable();

		env.set("functions",functions);

	}

	@Override
	public boolean onMsg(UserData user,Msg msg) {

		if (!msg.isCommand()) return false;

		LuaValue func = functions.get(msg.commandName());

		if (func.isfunction()) {

			try {

				Varargs result = func.invoke(LuaValue.varargsOf(new JavaInstance(user),new JavaInstance(msg)));

				StringBuilder reply = new StringBuilder();

				for (int index = 0;index < result.narg();index ++) {

					reply.append(result.arg(index + 1));

				}

				msg.send(reply.toString()).exec();


			} catch (LuaError err) {

				msg.send(err.toString()).exec();	

			}


			return true;

		}

		return false;

	}

	@Override
	public boolean onPrivMsg(UserData user,Msg msg) {

		if (!Env.FOUNDER.equals(user.userName)) return false;

		if (msg.isCommand()) {

			switch (msg.commandName()) {

				case "reset" : reset();break;
				case "addfunc" : addFunc(user,msg);break;
				case "remfunc": remFunc(user,msg);break;
				case "listfuncs" : listFuncs(user,msg);break;
				case "reload" : reload(user,msg);break;

				default : return false;

			}

			return true;

		} else {

			if (msg.text() != null) {

				try {

					LuaValue result = lua.load(msg.text()).call();

					if (!result.isnil()) {

						msg.send(result.toString()).exec();

					}

				} catch (LuaError err) {

					msg.send(err.toString()).exec();	

				}

			}

			return true;

		}

	}

	@Override
	public boolean onPoiPrivMsg(UserData user,Msg msg,CData point) {

		switch (point.getPoint()) {

			case POINT_INPUT_FUNC : onInputFunc(user,msg,point);break;

			default : return false;

		}

		return true;

	}


	final String POINT_INPUT_FUNC = "s|i";

	void addFunc(UserData user,Msg msg) {

		if (msg.commandParms().length != 1) {

			msg.send("/addfunc <fileName>").exec();

			return;

		}

		user.point = (cdata(POINT_INPUT_FUNC));

		user.point.setIndex(msg.commandParms()[0]);

		user.savePoint();

		msg.send("现在发送程式体 :").exec();

	}

	void remFunc(UserData user,Msg msg) {

		if (msg.commandParms().length != 1) {

			msg.send("/remfunc <fileName>").exec();

			return;

		}

		String fileName = msg.commandParms()[0] + ".lua";

		File func = new File(funcDir,fileName);

		if (func.exists()) {

			String content = FileUtil.readUtf8String(func);

			FileUtil.del(func);

			msg.send(fileName + " deleted").exec();

			msg.send(content).exec();

		}

	}


	void onInputFunc(UserData user,Msg msg,CData point) {

		String name = point.getIndex();

		String content = msg.text();

		try {

			lua.load(content);

		} catch (LuaError err) {

			msg.send(err.toString()).exec();

			return;

		}

		FileUtil.writeUtf8String(content,name + ".lua");

		user.point = null;

		user.savePoint();

		msg.send(name + ".lua saved").exec();

		reload(user,msg);

	}

	void listFuncs(UserData user,Msg msg) {

		File[] funcs = funcDir.listFiles();

		StringBuilder funcList = new StringBuilder();

		for (File func : funcs) {

			if (func.getName().endsWith(".lua")) {

				funcList.append(func.getName()).append("\n");

			}

		}

		msg.send(funcList.toString(),funcs.length + "funcs").exec();

	}

	void reload(UserData user,Msg msg) {

		long start = System.currentTimeMillis();

		functions = new LuaTable();

		File[] funcs = funcDir.listFiles();

		StringBuilder loaded = new StringBuilder();

		for (File func : funcs) {

			if (func.getName().endsWith(".lua")) {

				try {

					lua.loadfile(func.getPath()).call();

					loaded.append("loaded ").append(func.getName()).append("\n");

				} catch (LuaError err) {

					msg.send(err.toString()).exec();

				}

			}

		}

		long end = System.currentTimeMillis();

		msg.send(loaded.toString(),"time : " + (end - start) + "ms").exec();

	}

	class bind extends ZeroArgFunction {

		LinkedHashSet<String> packages = new LinkedHashSet<>();
		HashMap<String,Class<?>> loaded = new HashMap<>();
		
		@Override
		public LuaValue call() {

			LuaTable env = lua.get("_G").checktable();

			LuaTable mt = env.getmetatable().checktable();

			mt.set("__index",new __index());

			return NIL;

		}
		
		JavaClass matchClass(String simpleName) {
			
			for (String imported : packages) {
				
				try {
					
					Class.forName(imported + "." + simpleName);
					
				} catch (ClassNotFoundException e) {}

			}
			
		}

		class __index extends TwoArgFunction {

			@Override
			public LuaValue call(LuaValue T,LuaValue simpleName) {

				if (loaded.containsKey(simpleName)) {
					
					JavaClass clazz = JavaClass.forClass(loaded.get(simpleName));

					T.set(simpleName,clazz);
					
					return clazz;
					
				}
				
				return NIL;

			}
			
		}

	}

}
