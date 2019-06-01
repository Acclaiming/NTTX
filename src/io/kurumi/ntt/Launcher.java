package io.kurumi.ntt;

import cn.hutool.core.lang.*;
import cn.hutool.core.util.*;
import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.fragment.base.*;
import io.kurumi.ntt.fragment.bots.*;
import io.kurumi.ntt.fragment.twitter.auto.*;
import io.kurumi.ntt.fragment.twitter.login.*;
import io.kurumi.ntt.funcs.*;
import io.kurumi.ntt.funcs.admin.*;
import io.kurumi.ntt.funcs.nlp.*;
import io.kurumi.ntt.funcs.twitter.delete.*;
import io.kurumi.ntt.funcs.twitter.ext.*;
import io.kurumi.ntt.funcs.twitter.track.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.utils.*;
import java.io.*;
import java.util.*;

import cn.hutool.core.lang.Console;
import io.kurumi.ntt.twitter.archive.*;
import io.kurumi.ntt.fragment.twitter.status.*;
import io.kurumi.ntt.model.request.*;

public class Launcher extends BotFragment implements Thread.UncaughtExceptionHandler {

    public static final Launcher INSTANCE = new Launcher();

    @Override
    public boolean onMsg(UserData user,Msg msg) {

        if (super.onMsg(user,msg)) return true;

        if ("start".equals(msg.command()) && msg.params().length == 0) {

			msg.send("start failed successfully ~" ,Html.a("来玩 ᕙ(`▿´)ᕗ ！","https://t.me/joinchat/M5LsLFfkGoKE9Maqgi0HiA")).html().publicFailed();

            return true;

        } else if ("help".equals(msg.command())) {

            msg.send("没有帮助 ~").publicFailed();

			return true;

        }

        return false;

    }

	@Override
	public void start() {
		
		super.start();
		
		AutoTask.start();
		
		TrackTask.start();
		
		UserBotUI.start();
		
		Backup.start();
		
	}

	@Override
	public boolean silentStart() {
		
		if (super.silentStart()) {
			
			AutoTask.start();
			
			TrackTask.start();
			
			UserBotUI.start();
			
			Backup.start();
			
			return true;
			
		}
		
		return false;
		
	}
		

    @Override
    public void realStart() {

		// Base Functions
		
		addFragment(new PingFunction());
		
		addFragment(new GetIDs());
		
		addFragment(new Notice());
		
		addFragment(new DelMsg());
		
		addFragment(new Alias());
		
		addFragment(new Backup());
		
		addFragment(new Users());
		
		// Twitter
		
		addFragment(new StatusSearch());
		
		addFragment(new StatusGetter());
		
		addFragment(new StatusFetch());
		
		addFragment(new TwitterLogin());
		
		addFragment(new TwitterLogout());
		
		addFragment(new AutoUI());
		
		addFragment(new TrackUI());
		
		addFragment(new GroupRepeat());
		
		addFragment(new ChineseAction());
		
		addFragment(new AntiEsu());
		
		addFragment(new BanSetickerSet());
		
		addFragment(new AutoReply());
		
		addFragment(new TwitterDelete());
		
		// Bots
		
		addFragment(new UserBotUI());
		
		// Donate
		
		addFragment(new Donate());
		
			addFragment(new Final());
			
			super.realStart();
			
		/*
		
		new Timer().schedule(new TimerTask() {

				@Override
				public void run() {
					
					// if no task run , process will exit
					
				}
				
			},10 * 60 * 1000);
			
		*/

    }


	// public MtProtoBot mtp;

    @Override
    public void stop() {

		AutoTask.stop();
		
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

        Backup.stop();

        //  BotServer.INSTACNCE.stop();

    }



    public static void main(String[] args) {

        Thread.setDefaultUncaughtExceptionHandler(INSTANCE);

		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));

		if (!INSTANCE.isLongPulling()) {

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

		BotLog.info("正在启动...");
		
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

    }

    @Override
    public boolean onUpdate(UserData user,Update update) {
		
        BotLog.process(user,update);

		if (update.message() != null) {
			
			if (update.message().chat().type() == Chat.Type.Private && (user.contactable == null || !user.contactable)) {
				
				user.contactable = true;
				
				UserData.userDataIndex.put(user.id,user);
				
				UserData.data.setById(user.id,user);
				
			}
			
		}
		
        return false;

    }

    @Override
    public void uncaughtException(Thread thread,Throwable throwable) {

        BotLog.error("无法处理的错误,正在停止BOT",throwable);

		new Send(Env.GROUP,"NTT 异常退出",BotLog.parseError(throwable)).exec();
		
        INSTANCE.stop();

        System.exit(1);

    }

	@Override
	public boolean onCallback(UserData user,Callback callback) {
		
		if (callback.params.length == 0 || (callback.params.length == 1 && "null".equals(callback.params[0]))) {
			
			callback.confirm();
			
			return true;
			
		}
		
		return false;
		
	}

}
