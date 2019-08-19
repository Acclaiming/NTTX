package io.kurumi.ntt.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.AbstractLog;
import cn.hutool.log.level.Level;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.Launcher;
import io.kurumi.ntt.model.request.Send;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

public class BotLog extends AbstractLog {

	private static String logFormat = "[{level}] {name}: {msg}";

	private static Level currentLevel = Level.DEBUG;

	private String name;

	//------------------------------------------------------------------------- Constructor

	/**
	 * 构造
	 * 
	 * @param clazz 类
	 */
	public BotLog(Class<?> clazz) {

		this.name = (null == clazz) ? StrUtil.NULL : clazz.getSimpleName();

	}

	/**
	 * 构造
	 * 
	 * @param name 类名
	 */
	public BotLog(String name) {

		this.name = name;

	}

	@Override
	public String getName() {

		return this.name;

	}

	public static void setLevel(Level customLevel) {

		Assert.notNull(customLevel);
		currentLevel = customLevel;

	}

	//------------------------------------------------------------------------- Trace
	@Override
	public boolean isTraceEnabled() {

		return isEnabled(Level.TRACE);

	}

	@Override
	public void trace(String fqcn,Throwable t,String format,Object... arguments) {

		log(fqcn,Level.TRACE,t,format,arguments);

	}

	//------------------------------------------------------------------------- Debug

	@Override
	public boolean isDebugEnabled() {

		return isEnabled(Level.DEBUG);

	}

	@Override
	public void debug(String fqcn,Throwable t,String format,Object... arguments) {

		log(fqcn,Level.DEBUG,t,format,arguments);

	}

	//------------------------------------------------------------------------- Info
	@Override
	public boolean isInfoEnabled() {

		return isEnabled(Level.INFO);

	}

	@Override
	public void info(String fqcn,Throwable t,String format,Object... arguments) {

		log(fqcn,Level.INFO,t,format,arguments);

	}

	//------------------------------------------------------------------------- Warn
	@Override
	public boolean isWarnEnabled() {

		return isEnabled(Level.WARN);

	}

	@Override
	public void warn(String fqcn,Throwable t,String format,Object... arguments) {

		log(fqcn,Level.WARN,t,format,arguments);

	}

	//------------------------------------------------------------------------- Error
	@Override
	public boolean isErrorEnabled() {

		return isEnabled(Level.ERROR);

	}

	@Override
	public void error(String fqcn,Throwable t,String format,Object... arguments) {

		log(fqcn,Level.ERROR,t,format,arguments);

	}

	//------------------------------------------------------------------------- Log
	@Override
	public void log(String fqcn,Level level,Throwable t,String format,Object... arguments) {
		// fqcn 无效
		if (false == isEnabled(level)) {
			return;
		}

		final Dict dict = Dict.create()
			.set("date",DateUtil.now())
			.set("level",level.toString())
			.set("name",this.name)
			.set("msg",StrUtil.format(format,arguments));

		final String logMsg = StrUtil.format(logFormat,dict);

		if (level.ordinal() >= Level.WARN.ordinal()) {

			Console.error(t,logMsg);

			if (Launcher.INSTANCE == null) {

				if (Env.BOT_TOKEN == null) return;

				// 强制 初始化

				Launcher.INSTANCE = new Launcher() {

					@Override
					public String getToken() {

						return Env.BOT_TOKEN;

					}

				};
				
				Launcher.INSTANCE.init();

			}

			new Send(Env.LOG_CHANNEL,logMsg).async();

		} else {

			Console.log(t,logMsg);

		}

	}

	@Override
	public boolean isEnabled(Level level) {

		return currentLevel.compareTo(level) <= 0;

	}
	
	public static String parseError(Throwable error) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        error.printStackTrace(new PrintWriter(out, true));

        return StrUtil.utf8Str(out);

    }

}
