package io.kurumi.ntt;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.RuntimeUtil;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Update;
import io.kurumi.ntt.db.BotDB;
import io.kurumi.ntt.db.GroupData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.BotServer;
import io.kurumi.ntt.fragment.admin.Actions;
import io.kurumi.ntt.fragment.admin.Control;
import io.kurumi.ntt.fragment.admin.DelMsg;
import io.kurumi.ntt.fragment.admin.Notice;
import io.kurumi.ntt.fragment.admin.Stat;
import io.kurumi.ntt.fragment.admin.Users;
import io.kurumi.ntt.fragment.base.GetID;
import io.kurumi.ntt.fragment.base.PingFunction;
import io.kurumi.ntt.fragment.bots.BotChannnel;
import io.kurumi.ntt.fragment.bots.MyBots;
import io.kurumi.ntt.fragment.bots.NewBot;
import io.kurumi.ntt.fragment.bots.UserBot;
import io.kurumi.ntt.fragment.debug.Backup;
import io.kurumi.ntt.fragment.debug.DebugMsg;
import io.kurumi.ntt.fragment.debug.DebugStatus;
import io.kurumi.ntt.fragment.debug.DebugStickerSet;
import io.kurumi.ntt.fragment.debug.DebugUser;
import io.kurumi.ntt.fragment.group.AntiEsu;
import io.kurumi.ntt.fragment.group.BanSetickerSet;
import io.kurumi.ntt.fragment.group.GroupFunction;
import io.kurumi.ntt.fragment.group.GroupRepeat;
import io.kurumi.ntt.fragment.inline.MakeButtons;
import io.kurumi.ntt.fragment.inline.ShowSticker;
import io.kurumi.ntt.fragment.sticker.AddSticker;
import io.kurumi.ntt.fragment.sticker.MoveSticker;
import io.kurumi.ntt.fragment.sticker.NewStickerSet;
import io.kurumi.ntt.fragment.sticker.PackExport;
import io.kurumi.ntt.fragment.sticker.RemoveSticker;
import io.kurumi.ntt.fragment.sticker.StickerExport;
import io.kurumi.ntt.fragment.twitter.auto.AutoUI;
import io.kurumi.ntt.fragment.twitter.ext.MediaDownload;
import io.kurumi.ntt.fragment.twitter.ext.StatusGetter;
import io.kurumi.ntt.fragment.twitter.ext.TimelineUI;
import io.kurumi.ntt.fragment.twitter.ext.TwitterDelete;
import io.kurumi.ntt.fragment.twitter.ext.UserActions;
import io.kurumi.ntt.fragment.twitter.list.ListExport;
import io.kurumi.ntt.fragment.twitter.list.ListImport;
import io.kurumi.ntt.fragment.twitter.login.AuthExport;
import io.kurumi.ntt.fragment.twitter.login.TwitterLogin;
import io.kurumi.ntt.fragment.twitter.login.TwitterLogout;
import io.kurumi.ntt.fragment.twitter.status.StatusAction;
import io.kurumi.ntt.fragment.twitter.status.StatusFetch;
import io.kurumi.ntt.fragment.twitter.status.StatusSearch;
import io.kurumi.ntt.fragment.twitter.status.StatusUpdate;
import io.kurumi.ntt.fragment.twitter.status.TimedStatus;
import io.kurumi.ntt.fragment.twitter.track.TrackTask;
import io.kurumi.ntt.fragment.twitter.track.TrackUI;
import io.kurumi.ntt.fragment.twitter.track.UserTrackTask;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.BotLog;
import io.kurumi.ntt.utils.Html;
import java.io.File;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;
import io.kurumi.ntt.fragment.debug.DebugUF;
import io.kurumi.ntt.fragment.group.GroupOptions;

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

            msg.send("start failed successfully ~","","NTT是一只开源TelegramBot、可以作为Twitter客户端使用、也可以导出贴纸、创建私聊BOT、以及在群内沙雕发言与复读。","","BOT帮助文档请戳 : @NTT_X","交流群组在这里 : @NTTDiscuss","\n如果需要Telegram中文翻译，可以戳下面 :)",

										 "\n瓜体 ( @DuangCN ) : " + Html.a("      安装      ","https://t.me/setlanguage/duang-zh-cn"),

										 "\n简体中文二 ：" + Html.a("      安装      ","https://t.me/setlanguage/classic-zh-cn"),

										 "\n台湾正體 ：" + Html.a("      安装      ","https://t.me/setlanguage/taiwan"),

										 "\n台湾繁體 ：" + Html.a("      安装      ","https://t.me/setlanguage/zh-hant-beta"),

										 "\n香港繁體一 ：" + Html.a("      安装      ","https://t.me/setlanguage/hongkong"),

										 "\n香港繁體二 ：" + Html.a("      安装      ","https://t.me/setlanguage/zhhant-hk"),

										 "\n香港人口語 ：" + Html.a("      安装      ","https://t.me/setlanguage/hongkonger"),

										 "\n廣東話一 ：" + Html.a("      安装      ","https://t.me/setlanguage/zhhkpb1"),

										 "\n廣東話二 ：" + Html.a("      安装      ","https://t.me/setlanguage/hkcantonese")

										 ).html().publicFailed();

        } else if ("help".equals(function)) {

            msg.send("文档在 @NTT_X ~").publicFailed();

        } else if (!functions.containsKey(function) && msg.isPrivate()) {

						msg.send("没有这个命令 " + function,"查看文档 : @NTT_X").failedWith(10 * 1000);

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
				addFragment(new Actions());
				addFragment(new Stat());
				addFragment(new DebugMsg());
        addFragment(new Control());
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

        addFragment(new GroupRepeat());
				addFragment(new GroupOptions());
        //addFragment(new ChineseAction());
        //addFragment(new AntiEsu());
        addFragment(new BanSetickerSet());
				addFragment(new GroupFunction());

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
