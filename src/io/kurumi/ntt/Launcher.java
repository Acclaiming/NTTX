package io.kurumi.ntt;

import cn.hutool.core.lang.*;
import cn.hutool.core.util.*;
import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.fragment.admin.*;
import io.kurumi.ntt.fragment.base.*;
import io.kurumi.ntt.fragment.bots.*;
import io.kurumi.ntt.fragment.debug.*;
import io.kurumi.ntt.fragment.graph.*;
import io.kurumi.ntt.fragment.group.*;
import io.kurumi.ntt.fragment.idcard.*;
import io.kurumi.ntt.fragment.inline.*;
import io.kurumi.ntt.fragment.rss.*;
import io.kurumi.ntt.fragment.sticker.*;
import io.kurumi.ntt.fragment.twitter.auto.*;
import io.kurumi.ntt.fragment.twitter.ext.*;
import io.kurumi.ntt.fragment.twitter.list.*;
import io.kurumi.ntt.fragment.twitter.login.*;
import io.kurumi.ntt.fragment.twitter.status.*;
import io.kurumi.ntt.fragment.twitter.track.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.utils.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import cn.hutool.core.lang.Console;
import java.io.File;

public class Launcher extends BotFragment implements Thread.UncaughtExceptionHandler {

    public static final Launcher INSTANCE = new Launcher();

    public static void main(String[] args) {

        Thread.setDefaultUncaughtExceptionHandler(INSTANCE);

        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));

		//int serverPort = Integer.parseInt(Env.getOrDefault("server_port","-1"));
		String serverDomain = Env.get("server_domain");

		/*

		 while (serverPort == -1) {

		 System.out.print("输入本地Http服务器端口 : ");

		 try {

		 serverPort = Integer.parseInt(Console.input());

		 Env.set("server_port",serverPort);

		 } catch (Exception e) {
		 }

		 }

		 */

		if (serverDomain == null) {

			System.out.print("输入BotWebHook域名 : ");

			serverDomain = Console.input();

			Env.set("server_domain",serverDomain);

		}

		BotServer.INSTANCE = new BotServer(new File("/var/run/ntt.sock"),serverDomain);

		try {

			BotServer.INSTANCE.start();

		} catch (Exception e) {

			BotLog.error("端口被占用 请检查其他BOT进程。",e);

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

            } catch (Exception e) {
            }

		}

		RuntimeUtil.addShutdownHook(new Runnable() {

				@Override
				public void run() {

					INSTANCE.stop();

				}

			});

		INSTANCE.start();

	}

	public AtomicBoolean stopeed = new AtomicBoolean(false);

    static boolean initDB(String dbAddr,Integer dbPort) {

        try {

            BotDB.init(dbAddr,dbPort);

            return true;

        } catch (Exception e) {

            return false;

        }

    }

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerFunction("start","help");

	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		super.onFunction(user,msg,function,params);

        if ("start".equals(function)) {

            msg.send("start failed successfully ~","快戳咱的说明书 : https://manual.kurumi.io/").async();
			
			} else if ("help".equals(function)) {

            msg.send("快看说明书！ https://manual.kurumi.io/ ~").publicFailed();

        } else if (!functions.containsKey(function) && msg.isPrivate()) {

			msg.send("没有这个命令 " + function,"查看文档 : https://manual.kurumi.io/ ").failedWith(10 * 1000);

		}


	}

    @Override
    public void start() {

        super.start();

        startTasks();

    }


    // public MtProtoBot mtp;

    @Override
    public boolean silentStart() {

        if (super.silentStart()) {

            startTasks();

            return true;

        }

        return false;

    }

	UserTrackTask userTrackTask = new UserTrackTask();

	void startTasks() {

		TimedStatus.start();

		TimelineUI.start();

		TrackTask.start();

		UserBot.startAll();

		Backup.start();

		FeedFetchTask.start();

		userTrackTask.start();

	}

    @Override
    public void reload() {

		super.reload();

		// ADMIN

		addFragment(new BotChannnel());

		addFragment(new PingFunction());
		addFragment(new GetID());
		addFragment(new DelMsg());
        addFragment(new Notice());
        addFragment(new Backup());
        addFragment(new Users());
		addFragment(new Stat());
		addFragment(new DebugMsg());
 
		addFragment(new DebugUser());
        addFragment(new DebugStatus());
		addFragment(new DebugStickerSet());

		addFragment(new DebugUF());

		// Twitter

		addFragment(new TwitterLogin());
        addFragment(new TwitterLogout());
		addFragment(new UserActions());
		addFragment(new StatusUpdate());
		addFragment(new TimedStatus());
        addFragment(new StatusSearch());
        addFragment(new StatusGetter());
        addFragment(new StatusFetch());
		addFragment(new MediaDownload());
		addFragment(new AuthExport());
        addFragment(new AutoUI());
        addFragment(new TrackUI());
		addFragment(new StatusAction());
		addFragment(new TimelineUI());
		addFragment(new TwitterDelete());
		addFragment(new ListExport());
		addFragment(new ListImport());

		// GROUP

		addFragment(new GroupAdmin());
        addFragment(new GroupRepeat());
		addFragment(new GroupOptions());
        //addFragment(new AntiEsu());
        addFragment(new BanSetickerSet());
		addFragment(new GroupFunction());
		addFragment(new JoinCaptcha());

		addFragment(new RemoveKeyboard());

        // BOTS

        addFragment(new NewBot());
        addFragment(new MyBots());

		// SETS

		addFragment(new PackExport());
		addFragment(new StickerExport());
		addFragment(new NewStickerSet());
		addFragment(new AddSticker());
		addFragment(new RemoveSticker());
		addFragment(new MoveSticker());

		// INLINE

		addFragment(new MakeButtons());
		addFragment(new ShowSticker());

		// RSS

		addFragment(new RssSub());

		// IC

		addFragment(new Idcard());

    }

    @Override
    public void stop() {

		if (stopeed.getAndSet(true)) return;

		BotServer.INSTANCE.stop();

		mainTimer.cancel();

		trackTimer.cancel();

		userTrackTask.interrupt();

		GroupData.data.saveAll();

        for (BotFragment bot : BotServer.fragments.values()) {

            if (bot != this) {

				bot.stop();

			}

        }

        super.stop();

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
	public boolean onUpdate(final UserData user,final Update update) {

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

        BotLog.error("NTT出错",throwable);

        System.exit(1);

    }

}
