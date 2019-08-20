package io.kurumi.ntt;

import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.log.StaticLog;
import com.google.gson.Gson;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.ChatMember;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetChatMember;
import com.pengrad.telegrambot.response.GetChatMemberResponse;
import io.kurumi.ntt.cqhttp.TinxBot;
import io.kurumi.ntt.db.BotDB;
import io.kurumi.ntt.db.GroupData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.BotServer;
import io.kurumi.ntt.fragment.RpcApi;
import io.kurumi.ntt.fragment.admin.DelMsg;
import io.kurumi.ntt.fragment.admin.Notice;
import io.kurumi.ntt.fragment.admin.NoticePuhlish;
import io.kurumi.ntt.fragment.admin.Stat;
import io.kurumi.ntt.fragment.admin.StatusDel;
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
import io.kurumi.ntt.fragment.debug.DebugUF;
import io.kurumi.ntt.fragment.debug.DebugUser;
import io.kurumi.ntt.fragment.debug.Disappeared;
import io.kurumi.ntt.fragment.debug.GetRepliesTest;
import io.kurumi.ntt.fragment.dns.DNSLookup;
import io.kurumi.ntt.fragment.dns.WhoisLookup;
import io.kurumi.ntt.fragment.extra.Manchurize;
import io.kurumi.ntt.fragment.extra.ShowFile;
import io.kurumi.ntt.fragment.group.BanSetickerSet;
import io.kurumi.ntt.fragment.group.GroupActions;
import io.kurumi.ntt.fragment.group.GroupAdmin;
import io.kurumi.ntt.fragment.group.GroupFunction;
import io.kurumi.ntt.fragment.group.JoinCaptcha;
import io.kurumi.ntt.fragment.group.RemoveKeyboard;
import io.kurumi.ntt.fragment.group.options.OptionsMain;
import io.kurumi.ntt.fragment.idcard.Idcard;
import io.kurumi.ntt.fragment.inline.CoreValueEncode;
import io.kurumi.ntt.fragment.inline.MakeButtons;
import io.kurumi.ntt.fragment.inline.ShowSticker;
import io.kurumi.ntt.fragment.mods.PackageManager;
import io.kurumi.ntt.fragment.mstd.ui.MsMain;
import io.kurumi.ntt.fragment.netease.NeteaseMusic;
import io.kurumi.ntt.fragment.other.ZeroPadEncode;
import io.kurumi.ntt.fragment.qr.QrDecoder;
import io.kurumi.ntt.fragment.qr.QrEncoder;
import io.kurumi.ntt.fragment.rss.FeedFetchTask;
import io.kurumi.ntt.fragment.rss.RssSub;
import io.kurumi.ntt.fragment.sorry.MakeGif;
import io.kurumi.ntt.fragment.sticker.AddSticker;
import io.kurumi.ntt.fragment.sticker.MoveSticker;
import io.kurumi.ntt.fragment.sticker.NewStickerSet;
import io.kurumi.ntt.fragment.sticker.PackExport;
import io.kurumi.ntt.fragment.sticker.RemoveSticker;
import io.kurumi.ntt.fragment.sticker.StickerExport;
import io.kurumi.ntt.fragment.tests.MMPITest;
import io.kurumi.ntt.fragment.tinx.QQListener;
import io.kurumi.ntt.fragment.tinx.TelegramAdminFN;
import io.kurumi.ntt.fragment.tinx.TelegramFN;
import io.kurumi.ntt.fragment.tinx.TelegramListener;
import io.kurumi.ntt.fragment.twitter.archive.TEPH;
import io.kurumi.ntt.fragment.twitter.ext.MediaDownload;
import io.kurumi.ntt.fragment.twitter.ext.StatusGetter;
import io.kurumi.ntt.fragment.twitter.ext.TLScanner;
import io.kurumi.ntt.fragment.twitter.ext.TwitterDelete;
import io.kurumi.ntt.fragment.twitter.ext.UserActions;
import io.kurumi.ntt.fragment.twitter.list.ListExport;
import io.kurumi.ntt.fragment.twitter.status.StatusAction;
import io.kurumi.ntt.fragment.twitter.status.StatusDeleteTask;
import io.kurumi.ntt.fragment.twitter.status.StatusFetch;
import io.kurumi.ntt.fragment.twitter.status.StatusSearch;
import io.kurumi.ntt.fragment.twitter.status.StatusUpdate;
import io.kurumi.ntt.fragment.twitter.status.TimedStatus;
import io.kurumi.ntt.fragment.twitter.tasks.MargedNoticeTask;
import io.kurumi.ntt.fragment.twitter.tasks.TrackTask;
import io.kurumi.ntt.fragment.twitter.tasks.UserTrackTask;
import io.kurumi.ntt.fragment.twitter.ui.TimelineMain;
import io.kurumi.ntt.fragment.twitter.ui.TwitterMain;
import io.kurumi.ntt.maven.MvnDownloader;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.BotLog;
import io.kurumi.ntt.utils.BotLogFactory;
import io.kurumi.ntt.utils.Html;
import java.util.TimeZone;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Launcher extends BotFragment implements Thread.UncaughtExceptionHandler {

    public static Launcher INSTANCE;

	// public static OkHttpClient.Builder OKHTTP = new OkHttpClient.Builder();
	public static Gson GSON = new Gson();

	public static TinxBot TINX;

	public static Log log = LogFactory.get(Launcher.class);

    public static void main(String[] args) {

        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));

		LogFactory.setCurrentLogFactory(new BotLogFactory());

		long startAt = System.currentTimeMillis();

		log.debug("NTT 主程序正在启动 (๑•̀ㅂ•́)√");

        try {

            Env.init();

        } catch (Exception e) {

			log.error("配置文件格式错误",e);

            return;

        }

        if (Env.USE_UNIX_SOCKET) {

            BotServer.INSTANCE = new BotServer(Env.UDS_PATH,Env.SERVER_DOMAIN);

        } else {

            BotServer.INSTANCE = new BotServer(Env.LOCAL_PORT,Env.SERVER_DOMAIN);

        }

        try {

            BotDB.init(Env.DB_ADDRESS,Env.DB_PORT);

        } catch (Exception e) {

            log.error("MongoDB 连接失败",e);

            return;

        }

        try {

            BotServer.INSTANCE.start();

        } catch (Exception e) {

            log.error("本地HTTP服务器 启动失败",e);

            return;

        }

		TINX = new TinxBot(Env.CQHTTP_WS,Env.CQHTTP_URL);

		TINX.addListener(new QQListener());

		tryTinxConnect();

        INSTANCE = new Launcher() {

			@Override
			public String getToken() {

				return Env.BOT_TOKEN;

			}

		};

		log.info("正在启动本体 _(:з」∠)_");

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

		log.debug("正在挂载机器人托管");

		UserBot.startAll();

		log.debug("启动完成 用时 {}s _(:з」∠)_",(System.currentTimeMillis() - startAt) / 1000);

    }

	public static void tryTinxConnect() {

		try {

			TINX.start();

		} catch (Exception e) {

			log.debug(e,"CqHttp WebSocket 连接失败 正在等待重试");

			mainTimer.schedule(new TimerTask() {

					@Override
					public void run() {

						StaticLog.debug("正在重新连接 CqHttp WebSocket");

						tryTinxConnect();

					}

				},2 * 60 * 1000L);

		}



	}

    @Override
    public abstract String getToken();

    public AtomicBoolean stopeed = new AtomicBoolean(false);

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerFunction("start","help");

    }

    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params) {

        super.onFunction(user,msg,function,params);

        if ("start".equals(function)) {

            msg.send("start failed successfully ~\n{}",Env.HELP_MESSAGE).html().async();

        } else if ("help".equals(function)) {

            msg.send(Env.HELP_MESSAGE).html().publicFailed();

        } else if (!functions.containsKey(function) && msg.isPrivate()) {

            msg.send("没有这个命令 {}\n{}",function,Env.HELP_MESSAGE).html().failedWith(10 * 1000);

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

        TimelineMain.start();

        TrackTask.start();

		StatusDeleteTask.start();

		MargedNoticeTask.start();

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
		addFragment(new NoticePuhlish());
        addFragment(new DebugUser());
        addFragment(new DebugStatus());
        addFragment(new DebugStickerSet());

		addFragment(new StatusDel());
        addFragment(new DebugUF());

		addFragment(new GetRepliesTest());

        // GROUP

		addFragment(new GroupActions());
        addFragment(new GroupAdmin());
        addFragment(new OptionsMain());
        addFragment(new BanSetickerSet());
        addFragment(new GroupFunction());
        addFragment(new JoinCaptcha());
        addFragment(new RemoveKeyboard());


        // Twitter

		addFragment(new TwitterMain());

        addFragment(new UserActions());
        addFragment(new StatusUpdate());
        addFragment(new TimedStatus());
        addFragment(new StatusSearch());
        addFragment(new StatusGetter());
        addFragment(new StatusFetch());
        addFragment(new MediaDownload());
        addFragment(new StatusAction());
        addFragment(new TwitterDelete());
        addFragment(new ListExport());

        addFragment(new Disappeared());
        addFragment(new TEPH());

		addFragment(new TLScanner());

		// Mastodon

		addFragment(new MsMain());

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
		addFragment(new NeteaseMusic());
		addFragment(new ZeroPadEncode());
		addFragment(new QrDecoder());
		addFragment(new QrEncoder());
		addFragment(new DNSLookup());
		addFragment(new WhoisLookup());
		addFragment(new MMPITest());

        // QQ

		addFragment(new TelegramListener());
		addFragment(new TelegramFN());
		addFragment(new TelegramAdminFN());

		// Mods

		addFragment(new PackageManager());

		addFragment(new RpcApi());

    }

    @Override
    public void stop() {

        if (stopeed.getAndSet(true)) return;

        BotServer.INSTANCE.stop();

        mainTimer.cancel();

        trackTimer.cancel();

        userTrackTask.interrupt();

		TimelineMain.stop();

		StatusDeleteTask.stop();

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
	public int checkMsg(UserData user,Msg msg) {

		if (msg.newUser() != null && msg.newUser().id.equals(origin.me.id())) {

			return PROCESS_ASYNC_REJ;

		}

		return PROCESS_CONTINUE;

	}

	@Override
	public void onGroup(UserData user,Msg msg) {

		if (!msg.isSuperGroup()) {

			msg.reply("对不起, NTT 只能在 " + Html.b("超级群组") + " 工作, 如果需要继续, 请群组创建者将群组转换为超级群组再重新添加咱.").html().async();

			msg.exit();

			return;

		}

		GetChatMemberResponse resp = execute(new GetChatMember(msg.chatId(),origin.me.id().intValue()));

        ChatMember curr = resp.chatMember();

		if (resp.isOk() && curr.canRestrictMembers() != null && curr.canRestrictMembers() && curr.canDeleteMessages() != null && curr.canDeleteMessages()) {

			msg.reply("这里是NTT. 使用 /options 调出设置选单.").async();

		} else {

			msg.reply("很抱歉, NTT需要 " + Html.b("限制成员") + " 和 " + Html.b("删除消息") + " 权限以正常运行, 如果需要继续, 请管理员将机器人添加为管理员.").html().async();

			msg.exit();

		}

	}

    @Override
    public void uncaughtException(Thread thread,Throwable throwable) {

		log.error("出错 (全局)\n\n{}",BotLog.parseError(throwable));

        System.exit(1);

    }

}
