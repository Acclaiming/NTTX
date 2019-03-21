package io.kurumi.ntt.luaj;

import org.luaj.vm2.lib.*;
import java.lang.reflect.*;
import org.luaj.vm2.*;
import java.util.*;

public class LuaJavaMethod extends LuaTable {

	private Object obj;
	private Method[] methods;
	
	
	
	public LuaJavaMethod(Object obj,Method[] methods) {
		
		this.obj = obj;
		this.methods = methods;
		
	}
	
	/*

	@Override
	public Varargs invoke(Varargs args) {
		
		
		
	}
	
	private Object[] matchMethod(Method method,Varargs args) {
		
		Parameter[] params = method.getParameters();

		LinkedList<Object> result = new LinkedList<>();
		
		if ((params[params.length - 1].isVarArgs() && args.narg() >= params.length - 1) || args.narg() == params.length) {
			
			for (int index = 0;index < args.narg();index ++) {
				
				if (index < params.length - 1) {
					
					Parameter clazz = params[index];
					
					if (args.isnil(index) ) {}

				}
				
			}
			
			
		}

	}
	
	private Object parseVar(Class<?> clazz,Varargs args,int index) {
		
		if (args.isnil(index)) return null;
		
		LuaValue value = args.arg(index);
		
		if (clazz.equals(String.class)) {
			
			if (value instanceof LuaJavaObject) {
				
				LuaJavaObject lobj = (LuaJavaObject)value;
				
				if (lobj.obj instanceof String) {
				
					return (String)((LuaJavaObject)value).obj;
					
				}
				
			} else if (value instanceof LuaString) {
				
				return value.checkjstring();
				
			}
			
		} else if (clazz.equals(Integer.class)) {
			
			if (value instanceof LuaJavaObject) {

				LuaJavaObject lobj = (LuaJavaObject)value;

				if (lobj.obj instanceof Number) {

					return ((Integer)((LuaJavaObject)value).obj).intValue();

				}

			} else if (value instanceof LuaNumber) {
				
				return value.checknumber().toint();

			}

		
		} else if (clazz.equals(Double.class)) {
			
			if (value instanceof LuaJavaObject) {

				LuaJavaObject lobj = (LuaJavaObject)value;

				if (lobj.obj instanceof Number) {

					return ((Integer)((LuaJavaObject)value).obj).doubleValue();

				}

			} else if (value instanceof LuaNumber) {

				return value.checknumber().todouble();

			}
			
			
		} else if (clazz.equals(Long.class)) {
			
			if (value instanceof LuaJavaObject) {

				LuaJavaObject lobj = (LuaJavaObject)value;

				if (lobj.obj instanceof Number) {

					return ((Integer)((LuaJavaObject)value).obj).longValue();

				}

			} else if (value instanceof LuaNumber) {

				return value.checknumber().tolong();

			}
			
			
		} else if (clazz.equals(Float.class)) {
			
			if (value instanceof LuaJavaObject) {

				LuaJavaObject lobj = (LuaJavaObject)value;

				if (lobj.obj instanceof Boolean) {

					return lobj.obj;

				}

			} else if (value.isboolean()) {

				return value.checkboolean();

			}
			
			
		} else if(clazz.equals(Boolean.class)) {
			
			if (value instanceof LuaJavaObject) {

				LuaJavaObject lobj = (LuaJavaObject)value;

				if (lobj.obj instanceof Number) {

					return ((Integer)((LuaJavaObject)value).obj).intValue();

				}

			} else if (value instanceof LuaNumber) {

				return value.checknumber(index).toint();

			}
			
			
		} else {
			
			return args
			
		}
		
	}
	
	*/
	
}
