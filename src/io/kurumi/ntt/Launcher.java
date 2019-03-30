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

    public Launcher() {

        addFragment(Backup.INSTANCE);

        addFragment(GroupRepeat.INSTANCE);

		addFragment(TwitterUI.INSTANCE);

		addFragment(LuaEnv.INSTANCE);
		//addFragment(LuaEnv.INSTANCE.LuaFragmentOriginInstance);

        addFragment(TwitterArchive.INSTANCE);

        addFragment(StatusTrack.INSTANCE);
        
        addFragment(UserTrack.INSTANCE);

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
    public boolean onMsg(UserData user,Msg msg) {
     
        if (super.onMsg(user,msg)) return true;
        
        if ("start".equals(msg.command()) && msg.params().length == 0) {
            
            msg.send("欢迎dalao使用咱BOT！ ヾ(･ω･｀＝´･ω･)ﾉ♪ ").exec();
            msg.send("不加个裙玩吗 ~ " + Html.a("------ 戳这里！！！ ------","https://t.me/joinchat/H5gBQ1N2Mx5gf3Jm1e6RgQ")).html().exec();
            
            return true;
            
        }
        
        return false;
        
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

    @Override
    public void start() {
      
        super.start();
        
        new InitTask().start();
        
    }
    
}
