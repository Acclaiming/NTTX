package io.kurumi.nttools.twitter;

import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import io.kurumi.nttools.model.Callback;
import io.kurumi.nttools.model.Msg;
import io.kurumi.nttools.model.request.AbstractSend;
import io.kurumi.nttools.model.request.ButtonMarkup;
import io.kurumi.nttools.server.AuthCache;
import io.kurumi.nttools.twitter.ApiToken;
import io.kurumi.nttools.twitter.TwiAccount;
import io.kurumi.nttools.utils.UserData;
import java.util.LinkedList;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import io.kurumi.nttools.fragments.Fragment;
import twitter4j.Twitter;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import org.w3c.dom.UserDataHandler;

public class TwitterUI {

    public static final String COMMAND = "twitter";

    public static final String POINT_NEW_AUTH = "t|n";
    public static final String POINT_BACK = "t|b";
    public static final String POINT_MANAGE = "t|m";
    public static final String POINT_REMOVE = "t|r";
    public static final String POINT_CLEAN = "t|c";

    public static final String POINT_CLEAN_STATUS = "t|c|s";
    public static final String POINT_CLEAN_FOLLOWERS = "t|c|fo";
    public static final String POINT_CLEAN_FRIDENDS = "t|c|fr";
    public static final String POINT_CLEAN_ALL = "t|c|a";

    public static String help() {

        return "/twitter Twitter相关 ~";

    }

    public static void process(UserData userData, Msg msg) {

        if (!msg.isCommand() || !COMMAND.equals(msg.commandName())) return;

        main(userData, msg, false);

    }

    public static void main(final UserData userData, Msg msg, boolean edit) {

        Integer lastMsgId = userData.getByPath("twitter_ui.last_msg_id." + msg.fragment.name() + "." + userData.id(), Integer.class);

        if (lastMsgId != null && !edit) {

            msg.fragment.bot.execute(new DeleteMessage(userData.id(), lastMsgId));

        }

        AbstractSend send = null;

        String sendMsg = "这是Twitter盒子！有什么用呢？ (｡>∀<｡)";

        if (!edit) {

            send = msg.send(sendMsg);

        } else {

            send = msg.edit(sendMsg);

        }

        BaseResponse resp = send.buttons(new ButtonMarkup() {{

                    newButtonLine("认证新账号 (｡>∀<｡)", POINT_NEW_AUTH);

                    for (TwiAccount account : userData.getTwitterAccounts()) {

                        newButtonLine(account.name, POINT_MANAGE, userData, account);

                    }

                }}).exec();

        if (resp instanceof SendResponse) {

            userData.putByPath("twitter_ui.last_msg_id." + msg.fragment.name() + "." + userData.id(), ((SendResponse)resp).message().messageId());

        }
        
        userData.save();

    }

    public static void newAuth(final UserData user, final Callback callback) {

        callback.confirm();

        final Msg status = callback.send("正在请求认证链接 (｡>∀<｡)").send();

        try {

            final RequestToken requestToken = ApiToken.defaultToken.createApi().getOAuthRequestToken("https://" + callback.fragment.main.serverDomain + "/callback");

            AuthCache.cache.put(requestToken.getToken(), new AuthCache.Listener() {

                    @Override
                    public void onAuth(String oauthVerifier) {

                        try {

                            AccessToken accessToken =  ApiToken.defaultToken.createApi().getOAuthAccessToken(requestToken, oauthVerifier);

                            TwiAccount account = new TwiAccount(ApiToken.defaultToken.apiToken, ApiToken.defaultToken.apiSecToken, accessToken.getToken(), accessToken.getTokenSecret());

                            account.refresh();

                            LinkedList<TwiAccount> acc = user.getTwitterAccounts();

                            if (user.findUser(account.accountId) != null) {

                                status.edit("乃已经认证过这个账号了！ (ﾟ⊿ﾟ)ﾂ").exec();

                                return;

                            }

                            acc.add(account);

                            user.setTwitterAccounts(acc);

                            user.save();

                            status.edit("认证成功 (｡>∀<｡) 乃的账号", account.getFormatedName()).markdown().exec();

                            main(user, callback, false);

                        } catch (Exception e) {

                            status.edit(e.toString()).exec();

                        }

                    }

                });

            status.edit("请求成功 ╰(*´︶`*)╯\n 点这里认证 : ", requestToken.getAuthenticationURL()).exec();

        } catch (TwitterException e) {

            status.edit(e.toString()).exec();

        }


    }

    public static void manage(final UserData user, Callback callback) {

        final TwiAccount account = callback.data.getUser(user);

        callback.edit("(｡>∀<｡) 你好呀" +  account.name)
            .buttons(new ButtonMarkup() {{

                    newButtonLine("<< 返回上级 (*σ´∀`)σ", POINT_BACK);

                    newButtonLine("移除账号", POINT_REMOVE, user, account);

                    newButtonLine("账号清理 >>", POINT_CLEAN, user, account);

                }}).exec();

    }

    public static void remove(UserData user, Callback callback) {

        callback.text("已移除");

        LinkedList<TwiAccount> accounts = user.getTwitterAccounts();

        accounts.remove(callback.data.getUser(user));

        user.setTwitterAccounts(accounts);

        user.save();

        main(user, callback, true);

    }

    public static void clean(final UserData user, final Callback callback) {

        final TwiAccount account = callback.data.getUser(user);

        callback.edit("清理Twitter账号 [ 慎用 ！ ]", "注意 : 不可停止 、 不可撤销")
            .buttons(new ButtonMarkup() {{

                    newButtonLine("删推文", POINT_CLEAN_STATUS, user, account);
                    newButtonLine("删关注", POINT_CLEAN_FRIDENDS, user, account);
                    newButtonLine("删关注者", POINT_CLEAN_FOLLOWERS, user, account);
                    newButtonLine("全都要！", POINT_CLEAN_ALL, user, account);

                    newButtonLine("<< 返回账号", POINT_MANAGE, user, account);

                }}).exec();

    }

    public static void doClean(UserData userData, Callback callbeck, boolean status, boolean followers, boolean friends) {

        callbeck.text("正在开始...");

        new CleanThread(userData, callbeck, status, followers, friends).start();

    }


    public static void callback(UserData user, Callback callback) {

        switch (callback.data.getPoint()) {

                case POINT_NEW_AUTH : {

                    newAuth(user, callback);

                    return;

                }

                case POINT_MANAGE : {

                    manage(user, callback);

                    return;

                }

                case POINT_BACK : {

                    main(user, callback, true);

                    return;

                }

                case POINT_REMOVE : {

                    remove(user, callback);

                    return;

                }

                case POINT_CLEAN : {

                    clean(user, callback);

                    return;

                }

                case POINT_CLEAN_ALL : {

                    doClean(user, callback, true, true, true);

                    return;

                }

                case POINT_CLEAN_STATUS : {

                    doClean(user, callback, true, false, false);

                    return;

                }

                case POINT_CLEAN_FOLLOWERS : {

                    doClean(user, callback, false, true, false);

                    return;

                }

                case POINT_CLEAN_FRIDENDS : {

                    doClean(user, callback, false, false, true);

                    return;

                }

        }

    }

}
