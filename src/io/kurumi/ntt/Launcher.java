package io.kurumi.ntt;

import cn.hutool.core.lang.Console;
import com.mongodb.MongoClient;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.funcs.Backup;
import io.kurumi.ntt.funcs.GroupRepeat;
import io.kurumi.ntt.funcs.HideMe;
import io.kurumi.ntt.funcs.LuaEnv;
import io.kurumi.ntt.funcs.Maven;
import io.kurumi.ntt.funcs.Ping;
import io.kurumi.ntt.funcs.StatusTrack;
import io.kurumi.ntt.funcs.StickerManage;
import io.kurumi.ntt.funcs.TwitterArchive;
import io.kurumi.ntt.funcs.TwitterDelete;
import io.kurumi.ntt.funcs.TwitterUI;
import io.kurumi.ntt.funcs.UserTrack;
import io.kurumi.ntt.funcs.YourGroupRule;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.twitter.track.FTTask;
import io.kurumi.ntt.twitter.track.UTTask;
import io.kurumi.ntt.utils.BotLog;
import io.kurumi.ntt.utils.Html;
import java.util.TimeZone;
import io.kurumi.ntt.db.BotDB;
import com.mongodb.MongoException;
import io.kurumi.ntt.twitter.stream.SubTask;

public class Launcher extends BotFragment implements Thread.UncaughtExceptionHandler {

    public static final Launcher INSTANCE = new Launcher();

    public Launcher() {

        addFragment(Ping.INSTANCE);

        addFragment(StickerManage.INSTANCE);

        addFragment(TwitterDelete.INSTANCE);

        addFragment(Backup.INSTANCE);

        addFragment(GroupRepeat.INSTANCE);

		addFragment(TwitterUI.INSTANCE);

		addFragment(LuaEnv.INSTANCE);
		//addFragment(LuaEnv.INSTANCE.LuaFragmentOriginInstance);

        addFragment(TwitterArchive.INSTANCE);

        addFragment(StatusTrack.INSTANCE);

        addFragment(UserTrack.INSTANCE);

        addFragment(YourGroupRule.INSTANCE);

        // addFragment(AnalysisJsp.INSTANCE);

        addFragment(HideMe.INSTANCE);

        // addFragment(MusicSearch.INSTANCE);

        // addFragment(AntiTooManyMsg.INSTANCE);

        addFragment(Maven.INSTANCE);

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

        String dbAddr = Env.getOrDefault("db_address","127.0.0.1");
        Integer dbPort = Integer.parseInt(Env.getOrDefault("db_port","27017"));

        while (!initDB(dbAddr,dbPort)) {
            
            System.out.print("输入MongoDb地址 : ");
            dbAddr = Console.scanner().nextLine();

            try {

                System.out.print("输入MongoDb端口 : ");
                dbPort = Console.scanner().nextInt();

                Env.set("db_address",dbAddr);
                Env.set("db_port",dbPort);

            } catch (Exception e) {}

        }

        INSTANCE.start();


    }

    static boolean initDB(String dbAddr,Integer dbPort) {

        try {

            BotDB.init(dbAddr,dbPort);

            return true;

        } catch (MongoException e) {

            return false;

        }

    }

    @Override
    public String botName() {

        return "NTTBot";

    }

    @Override
    public boolean onMsg(UserData user,Msg msg) {

        if (super.onMsg(user,msg)) return true;

        if ("start".equals(msg.command()) && msg.params().length == 0) {

            msg.send(" ヾ(･ω･｀＝´･ω･)ﾉ♪ ").exec();
            msg.send("不加个裙玩吗 ~ " + Html.a("------ 戳这里！！！ ------","https://t.me/joinchat/H5gBQ1N2Mx4RuhIkq-EajQ")).html().exec();

            return true;

        }

        return false;

    }



    @Override
    public void uncaughtException(Thread thread,Throwable throwable) {

        BotLog.error("无法处理的错误,正在停止BOT",throwable);

        INSTANCE.bot().removeGetUpdatesListener();

        FTTask.stop();
        UTTask.stop();
        SubTask.stop();
        Backup.AutoBackupTask.INSTANCE.stop();

		//  BotServer.INSTACNCE.stop();

        System.exit(1);

    }

    @Override
    public boolean silentStart() {

        boolean result =  super.silentStart();

        if (result) {

            new InitTask().start();

        }

        return result;

    }

    @Override
    public void start() {

        super.start();

        new InitTask().start();

    }

    @Override
    public void stop() {

        FTTask.stop();
        UTTask.stop();

        super.stop();

    }



}
