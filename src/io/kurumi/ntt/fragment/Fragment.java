package io.kurumi.ntt.fragment;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.log.StaticLog;
import com.mongodb.client.FindIterable;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Poll;
import com.pengrad.telegrambot.model.Sticker;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ChatAction;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.GetFileResponse;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.Launcher;
import io.kurumi.ntt.db.PointData;
import io.kurumi.ntt.db.PointStore;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.status.StatusAction;
import io.kurumi.ntt.i18n.LocalString;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.Query;
import io.kurumi.ntt.model.request.Keyboard;
import io.kurumi.ntt.utils.BotLog;

import java.io.File;

public class Fragment {

    public static String PAYLOAD_SPLIT = "_";

    public BotFragment origin;

    public boolean update() {
        return false;
    }

    public boolean query() {
        return false;
    }


    public boolean poll() {
        return false;
    }

    public void onStop() {
    }

    public TelegramBot bot() {

        return origin.bot();

    }

    public boolean isLauncher() {

        return origin instanceof Launcher;

    }

    public boolean isMainInstance() {

        return origin == Launcher.INSTANCE;

    }

    public <T extends BaseRequest, R extends BaseResponse> R execute(BaseRequest<T, R> request) {

        return bot().execute(request);

    }

    public void executeAsync(BaseRequest request) {

        executeAsync(null, request);

    }

    public void executeAsync(Update update, final BaseRequest request) {

        if (update != null && update.lock != null && !update.lock.used.get()) {

            update.lock.send(request);

        } else {

            execute(request);

        }

    }


    public DeleteMessage deleteMessage(long chatId, int messageId) {

        return new DeleteMessage(chatId, messageId);

    }

    public PointStore point() {

        return origin.point();

    }

    public static void execute(final Runnable runnable) {

        execute(null, runnable);

    }

    public static void execute(final Update update, final Runnable runnable) {

        BotFragment.asyncPool.execute(new Runnable() {

            @Override
            public void run() {

                try {

                    runnable.run();

                } catch (Exception ex) {

                    StaticLog.get(getClass()).error("出错 (异步) {}\n\n{}", update == null ? "" : "\n\n" + new JSONObject(update.json).toStringPretty(), BotLog.parseError(ex));

                }

            }
        });

    }


    public PointData setPrivatePoint(Long userId, String pointTo, PointData content) {

        return point().setPrivate(userId, pointTo, content);

    }

    public PointData setPrivatePointData(Long userId, Msg command, String pointTo, Object content) {

        return point().setPrivateData(userId, command, pointTo, content);

    }

    public PointData setPrivatePoint(Long userId, String pointTo) {

        return point().setPrivateData(userId, null, pointTo, null);

    }

    public PointData setPrivatePointData(Long userId, String pointTo, Object content) {

        return point().setPrivateData(userId, null, pointTo, content);

    }

    public PointData setPrivatePoint(Long userId, Msg command, String pointTo) {

        return point().setPrivateData(userId, command, pointTo, null);

    }

    public PointData setGroupPoint(Long userId, String pointTo, PointData content) {

        return point().setGroup(userId, pointTo, content);

    }

    public PointData setGroupPointData(Long userId, Msg command, String pointTo, Object content) {

        return point().setGroupData(userId, command, pointTo, content);

    }

    public PointData setGroupPoint(Long userId, Msg command, String pointTo) {

        return point().setGroupData(userId, command, pointTo, null);

    }

    public PointData setGroupPointData(Long userId, String pointTo, Object content) {

        return point().setGroupData(userId, null, pointTo, content);

    }

    public PointData setGroupPoint(Long userId, String pointTo) {

        return point().setGroupData(userId, null, pointTo, null);

    }

    public PointData clearPrivatePoint(Long userId) {

        PointData toFinish = point().clearPrivate(userId);

        if (toFinish != null) toFinish.onFinish();

        return toFinish;
    }

    public PointData clearGroupPoint(Long userId) {

        PointData toFinish = point().clearGroup(userId);

        if (toFinish != null) toFinish.onFinish();

        return toFinish;

    }

    public PointData getPrivatePoint(Long userId) {

        return point().getPrivate(userId);

    }

    public PointData getGroupPoint(Long userId) {

        return point().getPrivate(userId);

    }


    public PointData setPrivatePoint(UserData user, String pointTo, PointData content) {

        return point().setPrivate(user.id, pointTo, content);

    }

    public PointData setPrivatePointData(UserData user, Msg command, String pointTo, Object content) {

        return point().setPrivateData(user.id, command, pointTo, content);

    }

    public PointData setPrivatePoint(UserData user, Msg command, String pointTo) {

        return point().setPrivateData(user.id, command, pointTo, null);

    }

    public PointData setPrivatePointData(UserData user, String pointTo, Object content) {

        return point().setPrivateData(user.id, null, pointTo, content);

    }

    public PointData setPrivatePoint(UserData user, String pointTo) {

        return point().setPrivateData(user.id, null, pointTo, null);

    }

    public PointData setGroupPoint(UserData user, String pointTo, PointData content) {

        return point().setGroup(user.id, pointTo, content);

    }

    public PointData setGroupPointData(UserData user, Msg command, String pointTo, Object content) {

        return point().setGroupData(user.id, command, pointTo, content);

    }

    public PointData setGroupPoint(UserData user, Msg command, String pointTo) {

        return point().setGroupData(user.id, command, pointTo, null);

    }

    public PointData setGroupPointData(UserData user, String pointTo, Object content) {

        return point().setGroupData(user.id, null, pointTo, content);

    }

    public PointData setGroupPoint(UserData user, String pointTo) {

        return point().setGroupData(user.id, null, pointTo, null);

    }

    public PointData clearPrivatePoint(UserData user) {

        PointData toFinish = point().clearPrivate(user.id);

        if (toFinish != null) toFinish.onFinish();

        return toFinish;
    }

    public PointData clearGroupPoint(UserData user) {

        PointData toFinish = point().clearGroup(user.id);

        if (toFinish != null) toFinish.onFinish();

        return toFinish;

    }

    public PointData getPrivatePoint(UserData user) {

        return point().getPrivate(user.id);

    }

    public PointData getGroupPoint(UserData user) {

        return point().getPrivate(user.id);

    }

    public boolean onUpdate(UserData user, Update update) {

        return false;

    }

    public void init(BotFragment origin) {

        this.origin = origin;

    }


    // 注册函数

    public void registerFunction(String... functions) {

        for (String function : functions) {

            origin.functions.put(function, this);

        }

    }

    public void registerAdminFunction(String... functions) {

        for (String function : functions) {

            origin.adminFunctions.put(function, this);

        }

    }

    public void registerPayload(String... payloads) {

        for (String payload : payloads) {

            origin.payloads.put(payload, this);

        }

    }

    public void registerAdminPayload(String... payloads) {

        for (String payload : payloads) {

            origin.adminPayloads.put(payload, this);

        }

    }

    public void registerPoint(String... points) {

        for (String point : points) {

            origin.points.put(point, this);

        }

    }

    public void registerCallback(String... points) {

        for (String point : points) {

            origin.callbacks.put(point, this);

        }


    }

    public int checkFunctionContext(UserData user, Msg msg, String function, String[] params) {

        return FUNCTION_PRIVATE;

    }

    public int checkFunction(UserData user, Msg msg, String function, String[] params) {

        return PROCESS_SYNC;

    }

    public void onFunction(UserData user, Msg msg, String function, String[] params) {
    }

    protected final String POINT_REQUEST_TWITTER = "request_twitter";

    static class TwitterRequest extends PointData {

        UserData fromUser;
        Msg originMsg;
        Fragment fragment;

        boolean payload;

    }

    public void requestTwitter(UserData user, Msg msg) {

        requestTwitter(user, msg, false, false);

    }

    public void requestTwitter(UserData user, Msg msg, boolean noCurrent) {

        requestTwitter(user, msg, noCurrent, false);

    }

    public void requestTwitterPayload(UserData user, Msg msg) {

        requestTwitter(user, msg, false, true);

    }

    public void requestTwitterPayload(UserData user, Msg msg, boolean noCurrent) {

        requestTwitter(user, msg, noCurrent, true);

    }

    public <T extends Fragment> T getInstance(Class<T> target) {

        for (Fragment fragment : origin.fragments) {

            if (target.isInstance(fragment)) {

                return (T) fragment;

            }

        }

        return null;

    }

    public void requestTwitter(final UserData user, final Msg msg, boolean noCurrent, final boolean isPayload) {

        if (!TAuth.contains(user.id)) {

            msg.send(LocalString.get(user).TWITTER_AUTH_NEED).exec();

            return;

        }

        final TAuth account;

        if (TAuth.data.countByField("user", user.id) == 1) {

            account = TAuth.getByUser(user.id).first();

        } else if (!noCurrent && StatusAction.current.containsId(user.id)) {

            account = TAuth.getById(StatusAction.current.getById(user.id).accountId);

        } else account = null;

        if (account != null && account.ign_target != null) {

            if (isPayload) {

                final String payload = msg.payload()[0];

                final String[] params = msg.payload().length > 1 ? ArrayUtil.sub(msg.payload(), 1, msg.payload().length) : new String[0];

                int checked = checkTwitterPayload(user, msg, payload, params, account);

                if (checked == PROCESS_ASYNC) {

                    execute(msg.update, new Runnable() {

                        @Override
                        public void run() {

                            onTwitterPayload(user, msg, payload, params, account);
                        }

                    });

                } else {

                    onTwitterPayload(user, msg, payload, params, account);

                }


            } else {

                int checked = checkTwitterFunction(user, msg, msg.command(), msg.fixedParams(), account);

                if (checked == PROCESS_ASYNC) {

                    execute(msg.update, new Runnable() {

                        @Override
                        public void run() {

                            onTwitterFunction(user, msg, msg.command(), msg.fixedParams(), account);

                        }

                    });

                } else {

                    onTwitterFunction(user, msg, msg.command(), msg.fixedParams(), account);

                }

            }

            return;

        }

        if (msg.isGroup()) {

            msg.send("咱已经在私聊回复了你。\n如果BOT有删除信息权限,命令和此回复将被自动删除。:)").failedWith();

            msg.targetChatId = user.id;

            msg.sendTyping();

        }

        final FindIterable<TAuth> accounts = TAuth.getByUser(user.id);

        TwitterRequest request = new TwitterRequest() {{

            this.fromUser = user;
            this.originMsg = msg;
            this.fragment = Fragment.this;
            this.payload = isPayload;

        }};

        msg.send("请选择目标账号 Σ( ﾟωﾟ ").keyboard(new Keyboard() {{

            for (TAuth account : accounts) {

                newButtonLine("@" + account.archive().screenName);

            }

        }}).withCancel().exec(request);


        setPrivatePoint(msg.from(), POINT_REQUEST_TWITTER, request);


    }

    public int checkTwitterFunction(UserData user, Msg msg, String function, String[] params, TAuth account) {

        return PROCESS_ASYNC;

    }

    public void onTwitterFunction(UserData user, Msg msg, String function, String[] params, TAuth account) {
    }

    public int checkPayload(UserData user, Msg msg, String payload, String[] params) {

        return PROCESS_ASYNC;

    }

    public void onPayload(UserData user, Msg msg, String payload, String[] params) {
    }

    public int checkTwitterPayload(UserData user, Msg msg, String payload, String[] params, TAuth account) {

        return PROCESS_ASYNC;

    }

    public void onTwitterPayload(UserData user, Msg msg, String payload, String[] params, TAuth account) {
    }

    public int checkPoint(UserData user, Msg msg, String point, PointData data) {

        return PROCESS_SYNC;

    }

    public void onPoint(UserData user, Msg msg, String point, PointData data) {
    }

    public int checkCallback(UserData user, Callback callback, String point, String[] params) {

        return PROCESS_SYNC;

    }

    public void onCallback(UserData user, Callback callback, String point, String[] params) {
    }

    // 基本函数

    public static final int PROCESS_REJECT = 0;
    public static final int PROCESS_SYNC = 1;
    public static final int PROCESS_ASYNC = 2;
    public static final int PROCESS_CONTINUE = 3;
    public static final int PROCESS_SYNC_REJ = 4;
    public static final int PROCESS_ASYNC_REJ = 5;
    public static final int PROCESS_SYNC_CONTINUE = 6;
    public static final int PROCESS_ASYNC_CONTINUE = 7;


    public static final int FUNCTION_PRIVATE = 1;
    public static final int FUNCTION_GROUP = 2;
    public static final int FUNCTION_PUBLIC = 3;

    public int checkMsg(UserData user, Msg msg) {

        return PROCESS_CONTINUE;

    }

    public void onMsg(UserData user, Msg msg) {

        if (msg.isGroup()) onGroup(user, msg);
        if (msg.isPrivate()) onPrivate(user, msg);

    }

    public void onGroup(UserData user, Msg msg) {
    }

    public void onPrivate(UserData user, Msg msg) {
    }

    public int checkChanPost(UserData user, Msg msg) {

        return PROCESS_CONTINUE;

    }

    public void onChanPost(UserData user, Msg msg) {
    }

    public void onPollUpdate(Poll poll) {
    }

    public void onQuery(UserData user, Query inlineQuery) {
    }

    public byte[] readStiker(Long userId, Sticker sticker) {

        File file = getFile(sticker.fileId());

        return FileUtil.readBytes(file);
    }

    public File getFile(String fileId) {

        File local = new File(Env.CACHE_DIR, "files/" + fileId);

        if (local.isFile()) return local;

        GetFileResponse file = execute(new GetFile(fileId));

        if (file == null || !file.isOk()) {

            return null;

        }

        String path = bot().getFullFilePath(file.file());

        HttpUtil.downloadFile(path, local);

        return local.isFile() ? local : null;

    }

    public File getFile(GetFileResponse file) {

        File local = new File(Env.CACHE_DIR, "files/" + file.file().fileId());

        if (local.isFile()) return local;

        if (!file.isOk()) {

            return null;

        }

        String path = bot().getFullFilePath(file.file());

        HttpUtil.downloadFile(path, local);

        return local;

    }

    public Msg sendSticker(long chatId, String sticker) {

        return Msg.from(this, execute(new SendSticker(chatId, sticker)));

    }

    public Msg sendFile(long chatId, String file) {

        return Msg.from(this, this.execute(new SendDocument(chatId, file)));

    }

    public Msg sendFile(long chatId, File file) {

        return Msg.from(this, execute(new SendDocument(chatId, file)));

    }

    public Msg sendFile(long chatId, byte[] file) {

        return Msg.from(this, execute(new SendDocument(chatId, file)));

    }

    public void sendTyping(long chatId) {

        execute(new SendChatAction(chatId, ChatAction.typing));

    }

    public void sendUpdatingFile(long chatId) {

        execute(new SendChatAction(chatId, ChatAction.upload_document));

    }

    public void sendUpdatingPhoto(long chatId) {

        execute(new SendChatAction(chatId, ChatAction.upload_photo));

    }

    public void sendUpdatingAudio(long chatId) {

        execute(new SendChatAction(chatId, ChatAction.upload_audio));

    }

    public void sendUpdatingVideo(long chatId) {

        execute(new SendChatAction(chatId, ChatAction.upload_video));

    }

    public void sendUpdatingVideoNote(long chatId) {

        execute(new SendChatAction(chatId, ChatAction.upload_video_note));

    }

    public void sendFindingLocation(long chatId) {

        execute(new SendChatAction(chatId, ChatAction.find_location));

    }

    public void sendRecordingAudio(long chatId) {

        execute(new SendChatAction(chatId, ChatAction.record_audio));

    }


    public void sendRecordingViedo(long chatId) {

        execute(new SendChatAction(chatId, ChatAction.record_video));

    }

    public void sendRecordingVideoNote(long chatId) {

        execute(new SendChatAction(chatId, ChatAction.record_video_note));

    }

    public void gc() {
    }


}
