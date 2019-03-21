package io.kurumi.ntt.luaj;

import cn.hutool.core.util.*;
import java.lang.reflect.*;
import org.luaj.vm2.*;
import java.util.*;

public class LuaJavaObject<T> extends LuaValue {

	public T obj;
	public Class<?> clazz;

	public LuaJavaObject(T obj) {

		this.obj = obj;
		this.clazz = obj.getClass();

	}

	@Override
	public int type() {

		return TTABLE;

	}

	@Override
	public boolean isfunction() {
		
		return obj instanceof Runnable;
		
	}

	@Override
	public LuaValue get(LuaValue key) {

		String strKey = key.checkjstring();

		Field field = ReflectUtil.getField(clazz,strKey);

		if (field != null) {

			try {

				return new LuaJavaObject(field.get(obj));

			} catch (Exception e) {

				throw new LuaError(e);

			}

		}

		Method[] methods = ReflectUtil.getMethods(clazz);

		LinkedList<Method> methodList = new LinkedList<>();

		for (Method method : methods) {

			if (method.getName().equals(strKey)) {

				method.setAccessible(true);

				methodList.add(method);

			}

		}

		if (!methodList.isEmpty()) {

			return new LuaJavaMethod(obj,methodList.toArray(new Method[methodList.size()]));

		}
		
		return null;

	}

	@Override
	public Varargs invoke(Varargs args) {
		
		throw new LuaError("无法调用Java对象");
		
	}

	@Override
	public String typename() {

		return "object";

	}

}
