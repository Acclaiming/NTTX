package io.kurumi.ntt.utils;

import cn.hutool.http.cookie.GlobalCookieManager;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.log.dialect.console.ConsoleLog;

public class BotLogFactory extends LogFactory {

	public BotLogFactory() {
		
		super("NTT Logging");
		
	}
	
	@Override
	public Log createLog(String name) {

		return new BotLog(name);
		
	}

	@Override
	public Log createLog(Class<?> clazz) {
		
		if (clazz.equals(GlobalCookieManager.class)) {

			return new ConsoleLog(clazz) {

				@Override public boolean isDebugEnabled() {

					return false;

				}

				@Override public void debug(String fqcn,Throwable t,String format,Object... arguments) {
				}

			};
			
		}
			
		return new BotLog(clazz);
		
	}

}
