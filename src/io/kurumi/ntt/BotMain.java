package io.kurumi.ntt;

import cn.hutool.log.StaticLog;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.server.BotServer;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.stickers.DVANG;
import io.kurumi.ntt.fragment.BotCallBackF;
import java.io.IOException;
import io.kurumi.ntt.utils.BotLog;
import io.kurumi.ntt.db.BotDB;
import cn.hutool.system.SystemUtil;
import cn.hutool.system.OsInfo;

public class BotMain extends BotFragment implements Thread.UncaughtExceptionHandler {

    public static final BotMain INSTANCE = new BotMain();

    public BotMain() {



    }

    public static void main(String[] args) {

        Thread.setDefaultUncaughtExceptionHandler(INSTANCE);
        
        BotServer.INSTACNCE.fragments.add(BotCallBackF.INSTANCE);

        try {

            BotServer.INSTACNCE.start();

        } catch (IOException e) {

            BotLog.error("端口被占用无法启动", e);

            return;

        }

        INSTANCE.start();
        
    }

    @Override
    public String botName() {

        return "NTTBot";

    }

    @Override
    public boolean isLongPulling() {

        return true;

    }

    @Override
    public boolean onMsg(UserData user, Msg msg) {

        msg.sendSticker(DVANG.发情);

        return true;

    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {

        StaticLog.error(throwable, "无法处理的错误");
        StaticLog.info("正在停止Bot");

        INSTANCE.stop();

        System.exit(1);
        
        // BotServer.INSTACNCE.stop();

    }

}
