package io.kurumi.ntt.funcs;

import io.kurumi.ntt.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.model.request.*;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.*;

public class LuaEnv extends Fragment {

	public static LuaEnv INSTANCE = new LuaEnv();

	public LuaTable env;
	public LuaTable functions = new LuaTable();

	public Globals lua; { reset(); }

	public void reset() {

		lua = JsePlatform.standardGlobals();

		env = lua.get("_G").checktable();

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

		if (Env.FOUNDER.equals(user.userName)) return false;

		if (msg.isCommand()) {

			switch (msg.commandName()) {
				
				case "reset" : reset(); break;
				
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




}
