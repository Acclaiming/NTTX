package io.kurumi.ntt;

import cn.hutool.core.util.RuntimeUtil;
import com.google.gson.Gson;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Update;
import io.kurumi.maven.MvnDownloader;
import io.kurumi.ntt.db.BotDB;
import io.kurumi.ntt.db.GroupData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.BotServer;
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
import io.kurumi.ntt.fragment.debug.AwtTest;
import io.kurumi.ntt.fragment.debug.Backup;
import io.kurumi.ntt.fragment.debug.DebugMsg;
import io.kurumi.ntt.fragment.debug.DebugStatus;
import io.kurumi.ntt.fragment.debug.DebugStickerSet;
import io.kurumi.ntt.fragment.debug.DebugUF;
import io.kurumi.ntt.fragment.debug.DebugUser;
import io.kurumi.ntt.fragment.debug.Disappeared;
import io.kurumi.ntt.fragment.extra.Manchurize;
import io.kurumi.ntt.fragment.extra.ShowFile;
import io.kurumi.ntt.fragment.group.BanSetickerSet;
import io.kurumi.ntt.fragment.group.GroupAdmin;
import io.kurumi.ntt.fragment.group.GroupFunction;
import io.kurumi.ntt.fragment.group.GroupOptions;
import io.kurumi.ntt.fragment.group.GroupRepeat;
import io.kurumi.ntt.fragment.group.JoinCaptcha;
import io.kurumi.ntt.fragment.group.RemoveKeyboard;
import io.kurumi.ntt.fragment.idcard.Idcard;
import io.kurumi.ntt.fragment.inline.CoreValueEncode;
import io.kurumi.ntt.fragment.inline.MakeButtons;
import io.kurumi.ntt.fragment.inline.ShowSticker;
import io.kurumi.ntt.fragment.rss.FeedFetchTask;
import io.kurumi.ntt.fragment.rss.RssSub;
import io.kurumi.ntt.fragment.sorry.MakeGif;
import io.kurumi.ntt.fragment.sticker.AddSticker;
import io.kurumi.ntt.fragment.sticker.MoveSticker;
import io.kurumi.ntt.fragment.sticker.NewStickerSet;
import io.kurumi.ntt.fragment.sticker.PackExport;
import io.kurumi.ntt.fragment.sticker.RemoveSticker;
import io.kurumi.ntt.fragment.sticker.StickerExport;
import io.kurumi.ntt.fragment.twitter.archive.TEPH;
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
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;
import okhttp3.OkHttpClient;
import io.kurumi.ntt.fragment.mstd.login.MsLogin;
import io.kurumi.ntt.fragment.mstd.login.MsLogout;

public abstract class Launcher extends BotFragment implements Thread.UncaughtExceptionHandler {

    public static Launcher INSTANCE;

	public static OkHttpClient.Builder OKHTTP = new OkHttpClient.Builder();
	public static Gson GSON = new Gson();
	
    public static void main(String[] args) {

        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
		
        try {

            Env.init();

        } catch (Exception e) {

            e.printStackTrace();

            return;

        }

        if (Env.USE_UNIX_SOCKET) {

            BotServer.INSTANCE = new BotServer(Env.UDS_PATH, Env.SERVER_DOMAIN);

        } else {

            BotServer.INSTANCE = new BotServer(Env.LOCAL_PORT, Env.SERVER_DOMAIN);

        }

        try {

            BotDB.init(Env.DB_ADDRESS, Env.DB_PORT);

        } catch (Exception ex) {

            ex.printStackTrace();

            return;

        }

        try {

            BotServer.INSTANCE.start();

        } catch (Exception e) {

            e.printStackTrace();

            return;

        }

        INSTANCE = new Launcher() {

			@Override
			public String getToken() {
				
				return Env.BOT_TOKEN;
				
			}
			
		};
		
        Thread.setDefaultUncaughtExceptionHandler(INSTANCE);

        RuntimeUtil.addShutdownHook(new Runnable() {

            @Override
            public void run() {

                INSTANCE.stop();

            }

        });

        INSTANCE.start();

		for (final String aliasToken : Env.ALIAS) {

			new Launcher() {

				@Override
				public String getToken() {

					return aliasToken;

				}

			}.start();

		}
		
    }

    @Override
    public abstract String getToken();

    public AtomicBoolean stopeed = new AtomicBoolean(false);

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerFunction("start", "help");

    }

    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params) {

        super.onFunction(user, msg, function, params);

        if ("start".equals(function)) {

			if (!isMainInstance()) {
				
				//msg.send("警告！这里是旧式实例，已经无法控制，请尽快切换到 @" + INSTANCE.me.username() + " :(").async();
				
				msg.send("这里是 NTT 的新位置 (数据互通哦 ~").async();
				
			}
			
            msg.send("start failed successfully ~\n", Env.HELP_MESSAGE).html().async();

        } else if ("help".equals(function)) {

            msg.send(Env.HELP_MESSAGE).html().publicFailed();

        } else if (!functions.containsKey(function) && msg.isPrivate()) {

            msg.send("没有这个命令 " + function, Env.HELP_MESSAGE).html().failedWith(10 * 1000);

        }


    }

    @Override
    public void start() {

        try {

            super.start();

        } catch (Exception e) {

            return;

        }

        if (isMainInstance()) startTasks();

    }

    @Override
    public boolean silentStart() throws Exception {

        if (isMainInstance() && super.silentStart()) {

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

        FeedFetchTask.start();

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
        addFragment(new Stat());
        addFragment(new DebugMsg());

        addFragment(new DebugUser());
        addFragment(new DebugStatus());
        addFragment(new DebugStickerSet());

        addFragment(new DebugUF());

        // GROUP

        addFragment(new GroupAdmin());
        addFragment(new GroupRepeat());
        addFragment(new GroupOptions());
        //addFragment(new AntiEsu());
        addFragment(new BanSetickerSet());
        addFragment(new GroupFunction());
        addFragment(new JoinCaptcha());

        addFragment(new RemoveKeyboard());


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

        addFragment(new Disappeared());
        addFragment(new TEPH());

		// Mastodon
		
		addFragment(new MsLogin());
		addFragment(new MsLogout());
		
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
		
		// Gif
		
		addFragment(new MakeGif());

        // Extra

        addFragment(new Manchurize());
        addFragment(new MvnDownloader());
		addFragment(new ShowFile());
		addFragment(new CoreValueEncode());

		addFragment(new AwtTest());
		
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
    public boolean onUpdate(final UserData user, final Update update) {

        if (update.message() != null) {

            if (update.message().chat().type() == Chat.Type.Private && (user.contactable == null || !user.contactable)) {

                user.contactable = true;

                UserData.userDataIndex.put(user.id, user);

                UserData.data.setById(user.id, user);

            }

        }


        return false;

    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {

        BotLog.error("NTT出错", throwable);

        System.exit(1);

    }

}
