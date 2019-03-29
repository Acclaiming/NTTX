package io.kurumi.ntt;

import cn.hutool.log.*;
import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.funcs.*;
import io.kurumi.ntt.utils.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.*;
import java.io.*;
import java.util.*;
import io.kurumi.ntt.twitter.track.FollowerTrackTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class Launcher extends BotFragment implements Thread.UncaughtExceptionHandler {

    public static final Launcher INSTANCE = new Launcher();

    public static AtomicBoolean initIng = new AtomicBoolean(false);

    public Launcher() {

        addFragment(Backup.INSTANCE);

        addFragment(GroupRepeat.INSTANCE);

		addFragment(TwitterUI.INSTANCE);

		addFragment(LuaEnv.INSTANCE);
		//addFragment(LuaEnv.INSTANCE.LuaFragmentOriginInstance);

        addFragment(TwitterArchive.INSTANCE);

        addFragment(TwitterTrack.INSTANCE);

        FollowerTrackTask.start();

        new InitTask().start();

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

        FollowerTrackTask.stop();

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

    String initMsg = "BOT正在初始化... 这可能需要几分钟的时间.";

	@Override
	public boolean onMsg(UserData user,Msg msg) {

        if (initIng.get() && msg.isCommand()) {

            msg.send(initMsg).exec();

            return true;

        }

        if (super.onMsg(user,msg)) return true;

		if (!(Env.FOUNDER.equals(user.userName) && msg.isCommand())) return false;

		switch (msg.command()) {

			case "stop" : stop();break;
			case "restart" : restart();break;
			case "reboot" : reboot();break;

			default : return false;

		}

		return true;

	}

    @Override
    public boolean onPPM(UserData user,Msg msg) {

        if (initIng.get()) {

            msg.send(initMsg).exec();

            return true;

        }

        return super.onPPM(user,msg);

    }

    @Override
    public boolean onCallback(UserData user,Callback callback) {

        if (initIng.get()) {

            callback.alert(initMsg);

            return true;

        }

        return super.onCallback(user,callback);

    }

    @Override
    public boolean onQuery(UserData user,Query inlineQuery) {

        if (initIng.get()) {

            inlineQuery.article("请稍后再使用...",initMsg).reply();

            return true;

        }

        return super.onQuery(user,inlineQuery);

    }

}
