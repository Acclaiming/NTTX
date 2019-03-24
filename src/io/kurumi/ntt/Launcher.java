package io.kurumi.ntt;

import cn.hutool.log.*;
import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.funcs.*;
import io.kurumi.ntt.utils.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.*;
import java.io.*;

public class Launcher extends BotFragment implements Thread.UncaughtExceptionHandler {

    public static final Launcher INSTANCE = new Launcher();

    public Launcher() {

        addFragment(GroupRepeat.INSTANCE);

		addFragment(TwitterUI.INSTANCE);

		addFragment(LuaEnv.INSTANCE);
		addFragment(LuaEnv.INSTANCE.LuaFragmentOriginInstance);

		addFragment(GroupButler.INSTANCE);

		addFragment(PingFunc.INSTANCE);

    }

    public static void main(String[] args) {

        Thread.setDefaultUncaughtExceptionHandler(INSTANCE);

        /*

		 BotServer.INSTACNCE.fragments.add(TGWebHookF.INSTANCE);

		 try {

		 BotServer.INSTACNCE.start();

		 } catch (IOException e) {

		 BotLog.error("端口被占用无法启动", e);

		 return;

		 }

		 */

        INSTANCE.start();

        BotLog.info("启动 成功 （￣～￣)");

    }

    @Override
    public String botName() {

        return "NTTBot";

    }

    @Override
    public void uncaughtException(Thread thread,Throwable throwable) {

        StaticLog.error(throwable,"无法处理的错误");
        StaticLog.info("正在停止Bot");

        INSTANCE.bot().removeGetUpdatesListener();

		//  BotServer.INSTACNCE.stop();

        System.exit(1);

    }

	public void stop() {

		try {

			Runtime.getRuntime().exec("service ntt stop");

		} catch (IOException e) {}

	}

	public void restart() {

		try {

			Runtime.getRuntime().exec("service ntt restart");

		} catch (IOException e) {}

	}

	@Override
	public boolean onGroupMsg(UserData user,Msg msg,boolean superGroup) {

		return false;

	}

}
