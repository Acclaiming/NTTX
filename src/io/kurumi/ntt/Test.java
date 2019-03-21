package io.kurumi.ntt;

import org.luaj.vm2.lib.jse.*;

public class Test {
	
	public static void main(String[] args) {
		
		JsePlatform.standardGlobals().load("print(\"hello\")").call();
		
	} 
	
}
