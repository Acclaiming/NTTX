package io.kurumi.ntt;

import cn.hutool.log.StaticLog;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.server.BotServer;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.db.UserData;

public class BotMain extends BotFragment implements Thread.UncaughtExceptionHandler {

    public static final BotMain INSTANCE = new BotMain();

    public static void main(String[] args) {
        
        Thread.setDefaultUncaughtExceptionHandler(INSTANCE);
        
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
        
        msg.sendTyping();
        
        msg.send(msg.text()).exec();
        
        return true;
        
    }
    
    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        
        StaticLog.error(throwable,"无法处理的错误");
        StaticLog.info("正在停止Bot");
        
        INSTANCE.stop();
        
       // BotServer.INSTACNCE.stop();
        
    }

}
