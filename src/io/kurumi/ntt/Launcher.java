package io.kurumi.ntt;

import cn.hutool.log.*;
import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.funcs.*;
import io.kurumi.ntt.utils.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.*;
import java.io.*;
import java.util.*;
import io.kurumi.ntt.twitter.track.TrackTask;

public class Launcher extends BotFragment implements Thread.UncaughtExceptionHandler {

    public static final Launcher INSTANCE = new Launcher();

    public Launcher() {

        addFragment(Backup.INSTANCE);
        
        addFragment(GroupRepeat.INSTANCE);

		addFragment(TwitterUI.INSTANCE);

		addFragment(LuaEnv.INSTANCE);
		//addFragment(LuaEnv.INSTANCE.LuaFragmentOriginInstance);

        addFragment(TwitterArchive.INSTANCE);
        
        addFragment(TwitterTrack.INSTANCE);
        
        TrackTask.INSTANCE.start();

    }

    public static void main(String[] args) {

        Thread.setDefaultUncaughtExceptionHandler(INSTANCE);

		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
		
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
        
        TrackTask.INSTANCE.stop();

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
	
	public void reboot() {

		try {

			Runtime.getRuntime().exec("reboot");

		} catch (IOException e) {}

	}
	
	
	@Override
	public boolean onMsg(UserData user,Msg msg) {

		if (!(Env.FOUNDER.equals(user.userName) && msg.isCommand())) return false;
		
		switch (msg.command()) {
			
			case "stop" : stop();break;
			case "restart" : restart();break;
			case "reboot" : reboot();break;
			
			default : return false;
			
		}
		
		return true;

	}

}
