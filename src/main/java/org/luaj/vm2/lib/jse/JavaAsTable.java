package org.luaj.vm2.lib.jse;

import java.util.*;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.*;

public class JavaAsTable extends OneArgFunction {

	@Override
	public LuaValue call(LuaValue arg) {
		
		if (arg.istable()) return arg;
		else if (arg.isuserdata()) {
			
			Object obj = arg.checkuserdata();

			if (obj instanceof Iterable || obj.getClass().isArray() || obj instanceof Map) {
			
				return CoerceJavaToLua.coerceX(obj);
				
			}
			
			throw new LuaError(obj.getClass().getName() + " can not be table");
			
		} else throw new LuaError(arg.typename() + " can not be table");

	}

}
