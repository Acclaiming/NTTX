/*******************************************************************************
 * Copyright (c) 2011 Luaj.org. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package org.luaj.vm2.lib.jse;

import cn.hutool.core.util.*;
import java.util.*;
import org.luaj.vm2.*;

/**
 * LuaValue that represents a Java instance of array type.
 * <p>
 * Can get elements by their integer key index, as well as the length.
 * <p>
 * This class is not used directly.  
 * It is returned by calls to {@link CoerceJavaToLua#coerce(Object)} 
 * when an array is supplied.
 * @see CoerceJavaToLua
 * @see CoerceLuaToJava
 */
public class JavaArray {

	public static LuaTable parseMap(Map map) {

		LuaTable table = new LuaTable();

		for (Map.Entry<Object,Object> entry : ((java.util.Map)map).entrySet()) {

			Object key = entry.getKey();

			Object value = entry.getValue();

			table.set(CoerceJavaToLua.coerce(key),CoerceJavaToLua.coerce(value));

		}

		return table;

	}

	public static LuaTable parseArray(Object object) {

		LuaTable table = new LuaTable();

		if (object instanceof Iterable) {
			
			for (Object obj :  ((Iterable)object)) {

				table.add(CoerceJavaToLua.coerce(obj));

			}
			
			return table;

		}

		int length = ArrayUtil.length(object);

		for (int index = 0;index < length;index ++) {

			table.add(CoerceJavaToLua.coerce(ArrayUtil.get(object,length)));

		}

		return table;

	}

}
