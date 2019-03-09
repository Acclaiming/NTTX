package io.kurumi.ntt;

import cn.hutool.log.StaticLog;
import io.kurumi.ntt.db.BotDB;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.TGWebHookF;
import io.kurumi.ntt.funcs.GroupRepeat;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.Msg;
// import io.kurumi.ntt.server.BotServer;
import io.kurumi.ntt.stickers.DVANG;
import io.kurumi.ntt.utils.BotLog;
import io.kurumi.ntt.utils.CData;
import java.io.IOException;
import io.kurumi.ntt.spam.SpamUI;

public class BotMain extends BotFragment implements Thread.UncaughtExceptionHandler {

    public static final BotMain INSTANCE = new BotMain();


    public BotMain() {

        addFragment(GroupRepeat.INSTANCE);
        addFragment(SpamUI.INSTANCE);

    }

    @Override
    public boolean onMsg(UserData user, Msg msg) {
     
        if (super.onMsg(user, msg)) return true;
        
        if ("woyaonvzhaung".equals(msg.commandName())) {
            
            msg.reply("是吗？").exec();
            
            return true;
            
        }
        
        return false;
        
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
    public void uncaughtException(Thread thread, Throwable throwable) {

        StaticLog.error(throwable, "无法处理的错误");
        StaticLog.info("正在停止Bot");

        INSTANCE.stop();

      //  BotServer.INSTACNCE.stop();

        System.exit(1);

    }

}
