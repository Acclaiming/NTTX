package io.kurumi.ntt.i18n;

import io.kurumi.ntt.td.TdApi;

public class Locale {
	
	public static Locale DEFAULT = new Locale();
	public static Locale ENG = new ENG();
	
	public static class ENG extends Locale {
		
		{
			
			
			
		}
		
	}
	
	public static Locale get(TdApi.User user) {
		
		if (user.languageCode.contains("zh")) {
			
			return DEFAULT;
			
		} else {
			
			return ENG;
			
		}
		
	}
	
}
