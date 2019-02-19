package io.kurumi.ntt;

import cn.hutool.log.StaticLog;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.TGWebHookF;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.server.BotServer;
import io.kurumi.ntt.twitter.TwiAuthF;
import io.kurumi.ntt.twitter.TwitterUI;
import io.kurumi.ntt.utils.BotLog;

import java.io.IOException;
import io.kurumi.ntt.stickers.DVANG;
import io.kurumi.ntt.utils.CData;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.db.BotDB;

public class BotMain extends BotFragment implements Thread.UncaughtExceptionHandler {

    public static final BotMain INSTANCE = new BotMain();

    public BotMain() {

        addFragment(TwitterUI.INSTANCE);

    }

    public static void main(String[] args) {

        Thread.setDefaultUncaughtExceptionHandler(INSTANCE);

        BotServer.INSTACNCE.fragments.add(TwiAuthF.INSTANCE);
        BotServer.INSTACNCE.fragments.add(TGWebHookF.INSTANCE);

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
    
    public boolean debug() {
        
        String debug = BotConf.get("DEBUG");

        return "true".equals(debug);
        
    }

    @Override
    public boolean onMsg(UserData user, Msg msg) {

        msg.send("Bot 正在 重写").exec();
        msg.sendSticker(DVANG.难受);

        return true;

    }

    @Override
    public boolean onPoiMsg(UserData user, Msg msg, CData point) {
        
        msg.send("Bot 正在 重写").exec();
        msg.sendSticker(DVANG.难受);

        return true;
        
    }

    @Override
    public boolean onCallback(UserData user, Callback callback) {
        
        callback.alert("BOT 正在 重写 >_<");
        
        return true;
        
    }

    @Override
    public boolean onPoiCallback(UserData user, Callback callback, CData point) {
        
        callback.alert("BOT 正在 重写 >_<");

        return true;

    }
     
    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {

        StaticLog.error(throwable, "无法处理的错误");
        StaticLog.info("正在停止Bot");

        BotDB.jedis.disconnect();
        
        INSTANCE.stop();

        BotServer.INSTACNCE.stop();

        System.exit(1);

    }

}
