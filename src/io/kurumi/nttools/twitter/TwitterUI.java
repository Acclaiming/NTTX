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


    public void process(UserData userData, Msg msg) {

        main(userData, msg, false);

    }

    public void main(final UserData userData, Msg msg, boolean edit) {

        Integer lastMsgId = userData.getByPath("twitter_ui.last_msg_id." + msg.fragment.name() + "." + userData.id(), Integer.class);

        if (lastMsgId != null) {

            msg.fragment.bot.execute(new DeleteMessage(userData.id(), lastMsgId));

        }

        AbstractSend send = null;

        String sendMsg = "这是Twitter盒子！有什么用呢？ (｡>∀<｡)";

        if (!edit) {

            msg.send(sendMsg);

        } else {

            msg.edit(sendMsg);

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

    }

    public void newAuth(final UserData user, final Callback callback) {

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

                            if (acc.contains(account)) {

                                status.edit("乃已经认证过这个账号了！ (ﾟ⊿ﾟ)ﾂ").exec();

                                return;

                            }

                            acc.add(account);

                            user.setTwitterAccounts(acc);

                            user.save();

                            status.edit("认证成功 (｡>∀<｡) 乃的账号", account.getFormatedName()).markdown().exec();



                            main(user, callback, true);

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

    public void manage(final UserData user, Callback callback) {

        final TwiAccount account = callback.data.getUser(user);

        callback.edit("(｡>∀<｡) 你好呀" +  account.name)
            .buttons(new ButtonMarkup() {{

                    newButtonLine("<< 返回上级 (*σ´∀`)σ", POINT_BACK);

                    newButtonLine("移除账号", POINT_REMOVE, user, account);

                    newButtonLine("账号清理 >>", POINT_CLEAN, user, account);

                }}).exec();

    }

    public void remove(UserData user, Callback callback) {

        callback.text("已移除");

        LinkedList<TwiAccount> accounts = user.getTwitterAccounts();

        accounts.remove(callback.data.getUser(user));

        user.setTwitterAccounts(accounts);

        user.save();

        main(user, callback, true);

    }

    public void clean(final UserData user, final Callback callback) {

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
    
    public void doClean(UserData userData) {}

    public void callback(UserData user, Callback callback) {

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

                }

        }

    }

}
