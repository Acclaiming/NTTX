package io.kurumi.ntt;

import cn.hutool.log.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.funcs.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.utils.*;
import io.kurumi.ntt.luaj.*;
import org.luaj.vm2.*;

public class Launcher extends BotFragment implements Thread.UncaughtExceptionHandler {

    public static final Launcher INSTANCE = new Launcher();

    public Launcher() {

        addFragment(GroupRepeat.INSTANCE);

    }

    @Override
    public boolean onMsg(UserData user,Msg msg) {

        if (super.onMsg(user,msg)) return true;

        if ("woyaonvzhaung".equals(msg.commandName())) {

            msg.reply("是吗？").exec();

            return true;

        }


        return false;

	}

	@Override
	public boolean onPrivMsg(UserData user,Msg msg) {

		LuaDaemon lua = LuaDaemon.get(user);

		try {

			msg.send(lua.exec(msg.text()).toString()).exec();

		} catch (LuaError err) {

			msg.send(err.toString()).exec();	

		}

		return true;

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

        INSTANCE.stop();

		//  BotServer.INSTACNCE.stop();

        System.exit(1);

    }

}
