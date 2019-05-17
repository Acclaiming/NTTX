package io.kurumi.ntt;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.RuntimeUtil;
import com.mongodb.MongoException;
import com.pengrad.telegrambot.model.ChatMember;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetChatAdministrators;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.db.BotDB;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.BotServer;
import io.kurumi.ntt.funcs.AntiHalal;
import io.kurumi.ntt.funcs.Backup;
import io.kurumi.ntt.funcs.BanSetickerSet;
import io.kurumi.ntt.funcs.ChineseAction;
import io.kurumi.ntt.funcs.GroupRepeat;
import io.kurumi.ntt.funcs.Ping;
import io.kurumi.ntt.funcs.RiceCakeMeme;
import io.kurumi.ntt.funcs.StickerManage;
import io.kurumi.ntt.funcs.Utils;
import io.kurumi.ntt.funcs.admin.Notice;
import io.kurumi.ntt.funcs.chatbot.ForwardMessage;
import io.kurumi.ntt.funcs.nlp.AutoReply;
import io.kurumi.ntt.funcs.twitter.TwitterFunctions;
import io.kurumi.ntt.funcs.twitter.ext.BioSearch;
import io.kurumi.ntt.funcs.twitter.track.TrackTask;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.BotLog;
import io.kurumi.ntt.utils.Html;
import java.io.IOException;
import java.util.List;
import java.util.TimeZone;
import io.kurumi.ntt.funcs.*;
import io.kurumi.ntt.kernel.*;
import java.lang.reflect.*;
import org.telegram.bot.structure.*;
import org.telegram.api.functions.photos.*;
import org.telegram.api.functions.messages.*;

public class Launcher extends BotFragment implements Thread.UncaughtExceptionHandler {

    public static final Launcher INSTANCE = new Launcher();

    @Override
    public boolean onMsg(UserData user,Msg msg) {

		
        if (super.onMsg(user,msg)) return true;

        if ("start".equals(msg.command()) && msg.params().length == 0) {

            if (msg.isPrivate()) {

                msg.send(" ヾ(･ω･｀＝´･ω･)ﾉ♪ ").exec();
                         msg.send("输入 / 就有命令补全啦 ~ 在 @NTTPublic 查看帮助 ~").exec();
                msg.send("开源地址在 " + Html.a("NTTools","https://github.com/HiedaNaKan/NTTools") + " 欢迎打心 (๑´ڡ`๑)").html().exec();

            } else {

                msg.send("start failed successfully ᕙ(`▿´)ᕗ").publicFailed();

            }

            return true;

        } else if ("help".equals(msg.command())) {

            msg.send("请关注 @NTTPublic (๑˃̵ᴗ˂̵)و").publicFailed();

        } else if ("discuss".equals(msg.command())) {

            msg.send("你群已删 请加讨论群 @NTTIssues").html().exec();


        }

        return false;

    }
    
    @Override
    public void realStart() {

        /*

         FTTask.start();
         UTTask.start();
         SubTask.start();

         */

		addFragment(AntiEsu.INSTANCE);
		
        addFragment(Ping.INSTANCE);
		
		addFragment(GetIDs.INSTANCE);

        addFragment(Utils.INSTANCE);

        addFragment(StickerManage.INSTANCE);

        // addFragment(TwitterDelete.INSTANCE);

        addFragment(Backup.INSTANCE);

		
		
        addFragment(GroupRepeat.INSTANCE);
		
		
        // addFragment(TwitterArchive.INSTANCE);

        //  addFragment(FollowersTrack.INSTANCE);

        //  addFragment(UserTrack.INSTANCE);

        //  addFragment(StatusUI.INSTANCE);

        addFragment(ChineseAction.INSTANCE);

        addFragment(BanSetickerSet.INSTANCE);

        addFragment(AntiHalal.INSTANCE);

        // addFragment(HideMe.INSTANCE);

        addFragment(ForwardMessage.INSTANCE);

        // addFragment(BlockList.INSTANCE);

        addFragment(BioSearch.INSTANCE);

        addFragment(Notice.INSTANCE);

        addFragment(RiceCakeMeme.INSTANCE);
        
        addFragment(AutoReply.INSTANCE);

        TwitterFunctions.init(this);

       TrackTask.start();

        Backup.AutoBackupTask.INSTANCE.start();

       super.realStart();
		
	   /*
	   
		mtp = new MtProtoBot(getToken(),this);

		try {
			
			System.out.println("LOGIN :" + mtp.getConfig().getBotToken());
			
			System.out.println("LOGIN : " + mtp.init());
			
			mtp.startBot();
			
		} catch (Exception e) {
			
			BotLog.info("login error",e);
			
		}
		
		
		*/
		
        ForwardMessage.start();

        BotLog.info("初始化 完成 :)");

    }
	
	
	// public MtProtoBot mtp;
    
    @Override
    public void stop() {

		// mtp.stopBot();
		
        for (BotFragment bot : BotServer.fragments.values()) {

            if (bot != this) bot.stop();

        }

        super.stop();

        BotServer.INSTANCE.stop();

        /*

         FTTask.stop();
         UTTask.stop();
         SubTask.stopAll();

         */

        TrackTask.stop();

        Backup.AutoBackupTask.INSTANCE.stop();

        //  BotServer.INSTACNCE.stop();

    }
    
    
    
    public static void main(String[] args) {

        Thread.setDefaultUncaughtExceptionHandler(INSTANCE);

		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));

        int serverPort = Integer.parseInt(Env.getOrDefault("server_port","-1"));
        String serverDomain = Env.get("server_domain");

        while (serverPort == -1) {

            System.out.print("输入本地Http服务器端口 : ");

            try {

                serverPort = Integer.parseInt(Console.input());

                Env.set("server_port",serverPort);

            } catch (Exception e) {}

        }

        if (serverDomain == null) {

            System.out.print("输入BotWebHook域名 : ");

            serverDomain = Console.input();

            Env.set("server_domain",serverDomain);

        }

        BotServer.INSTANCE = new BotServer(serverPort,serverDomain);

        try {

            BotServer.INSTANCE.start();

        } catch (IOException e) {

            BotLog.error("端口被占用 请检查其他BOT进程。");

            return;

        }

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

        RuntimeUtil.addShutdownHook(new Runnable() {

                @Override
                public void run() {

                    INSTANCE.stop();

                }

            });

        INSTANCE.start();

    }

    static boolean initDB(String dbAddr,Integer dbPort) {

        try {

            BotDB.init(dbAddr,dbPort);

            return true;

        } catch (Exception e) {

            return false;

        }

    }

    @Override
    public String botName() {

        return "NTTBot";

    }

    @Override
    public boolean isLongPulling() {

        return false;

        // 否则 NanoHttpd 会 无端 停止。

    }

    @Override
    public boolean onUpdate(UserData user,Update update) {

        BotLog.process(user,update);

        return false;

    }

    @Override
    public void uncaughtException(Thread thread,Throwable throwable) {
        
        BotLog.error("无法处理的错误,正在停止BOT",throwable);

        INSTANCE.stop();
        
        System.exit(1);

    }




}
