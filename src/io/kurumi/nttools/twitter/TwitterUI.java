package io.kurumi.nttools.twitter;

import com.pengrad.telegrambot.response.BaseResponse;
import io.kurumi.nttools.fragments.FragmentBase;
import io.kurumi.nttools.model.Callback;
import io.kurumi.nttools.model.Msg;
import io.kurumi.nttools.model.request.AbstractSend;
import io.kurumi.nttools.model.request.ButtonMarkup;
import io.kurumi.nttools.model.request.Send;
import io.kurumi.nttools.server.AuthCache;
import io.kurumi.nttools.twitter.ApiToken;
import io.kurumi.nttools.twitter.TwiAccount;
import io.kurumi.nttools.utils.UserData;
import java.util.Date;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import io.kurumi.nttools.Launcher;
import io.kurumi.nttools.server.BotServer;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.core.util.CharsetUtil;
import java.util.HashMap;

public class TwitterUI extends FragmentBase {

    public static final TwitterUI INSTANCE = new TwitterUI();

    public static String help =  "/twitter Twitter相关 ~";

    private static final String COMMAND = "twitter";

    private static final String POINT_NEW_AUTH = "t|n";
    private static final String POINT_BACK = "t|b";
    private static final String POINT_MANAGE = "t|m";
    private static final String POINT_REMOVE = "t|r";
    private static final String POINT_CLEAN = "t|c";

    private static final String POINT_CLEAN_STATUS = "t|c|s";
    private static final String POINT_CLEAN_FOLLOWERS = "t|c|fo";
    private static final String POINT_CLEAN_FRIDENDS = "t|c|fr";
    private static final String POINT_CLEAN_ALL = "t|c|a";

    private static final String POINT_INPUT_CALLBACK_URL = "t|ic";

    @Override
    public boolean processPrivateMessage(UserData user, Msg msg) {

        if (user.point != null) {

            switch (user.point.getPoint()) {

                    case POINT_INPUT_CALLBACK_URL : onInputCallbackUrl(user, msg);break;

                    default : return false;

            }

            return true;

        }

        if (!msg.isCommand() || !COMMAND.equals(msg.commandName())) return false;

        main(user, msg, false);

        return true;

    }

    public void main(final UserData user, Msg msg, boolean edit) {

        deleteLastSend(user, msg, "twitter_ui");

        AbstractSend send = null;

        String sendMsg = "这是Twitter盒子！有什么用呢？ (｡>∀<｡)";

        if (!edit) {

            send = msg.send(sendMsg);

        } else {

            send = msg.edit(sendMsg);

        }

        BaseResponse resp = send.buttons(new ButtonMarkup() {{

                    newButtonLine("认证新账号 (｡>∀<｡)", POINT_NEW_AUTH);

                    for (TwiAccount account : user.twitterAccounts) {

                        newButtonLine(account.name, POINT_MANAGE, user, account);

                    }

                }}).exec();

        saveLastSent(user, msg, "twitter_ui", resp);

    }

    public void newAuth(final UserData user, final Callback callback) {

        if (user.twitterAccounts.size() > 0) {

            callback.alert("由于Bot内部处理问题，用户不能添加更多账号");

            return;

        }

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

                            for (UserData u : callback.fragment.main.getUsers()) {

                                if (u.twitterAccounts.contains(account)) {

                                    if (!u.equals(user)) {

                                        new Send(callback.fragment, u.id, "您的Twitter账号 " + account.getFormatedNameMarkdown(), "已经被 @" + user.userName + "认证 已从您的列表移除", "如果这不是您本人的操作 请立即修改Twitter密码并在 [ 账号 -> 应用和会话 ] 取消不信任的应用链接", new Date().toLocaleString()).markdown().exec();

                                    }

                                    u.twitterAccounts.remove(account);

                                }
                                
                                u.save();

                            }

                            user.twitterAccounts.add(account);

                            user.save();

                            status.edit("认证成功 (｡>∀<｡) 乃的账号", account.getFormatedName()).markdown().exec();
                            
                            if (BotServer.INSTANCE == null) {
                                
                                user.point = null;
                                user.save();
                                
                            }

                            main(user, callback, false);
                            
                            AuthCache.cache.remove(requestToken.getToken());

                        } catch (Exception e) {

                            if (BotServer.INSTANCE == null) {
                                
                                callback.send("输入的Url有误或已失效 请重新输入","或使用 /cancel 退出 (｡>∀<｡)").exec();
                                
                            }
                            
                            status.edit(e.toString()).exec();

                        }

                    }

                });

            status.edit("请求成功 ╰(*´︶`*)╯\n 点这里认证 : ", requestToken.getAuthenticationURL()).exec();

            if (BotServer.INSTANCE == null) {

                callback.send("抱歉 由于Bot正暂时运行在非公网上 无法得到认证的回调", "请在认证完成之后 复制 跳转到的Url(链接) 发送给Bot即可完成验证 (｡>∀<｡)").exec();

                user.point = cdata(POINT_INPUT_CALLBACK_URL);

                user.save();

            }

        } catch (TwitterException e) {

            status.edit(e.toString()).exec();

        }


    }

    public void onInputCallbackUrl(UserData user, Msg msg) {

        String url = msg.text();

        HashMap<String, String> params = HttpUtil.decodeParamMap(url, CharsetUtil.UTF_8);

        String requestToken = params.get("oauth_token");
        String oauthVerifier = params.get("oauth_verifier");
        
        if (AuthCache.cache.containsKey(requestToken)) {
            
            AuthCache.cache.get(requestToken).onAuth(oauthVerifier);
            
        } else {
            
            msg.send("Bot可能已经重启...","请重新点击新认证 (｡>∀<｡)").exec();
            
            user.point = null;
            user.save();
            
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

        user.twitterAccounts.remove(callback.data.getUser(user));

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

    public void doClean(UserData userData, Callback callbeck, boolean status, boolean followers, boolean friends) {

        callbeck.text("正在开始...");

        new CleanThread(userData, callbeck, status, followers, friends).start();

    }

    @Override
    public boolean processCallbackQuery(UserData user, Callback callback) {

        switch (callback.data.getPoint()) {

                case POINT_NEW_AUTH : {

                    newAuth(user, callback);

                    return true;

                }

                case POINT_MANAGE : {

                    manage(user, callback);

                    return true;

                }

                case POINT_BACK : {

                    main(user, callback, true);

                    return true;

                }

                case POINT_REMOVE : {

                    remove(user, callback);

                    return true;

                }

                case POINT_CLEAN : {

                    clean(user, callback);

                    return true;

                }

                case POINT_CLEAN_ALL : {

                    doClean(user, callback, true, true, true);

                    return true;

                }

                case POINT_CLEAN_STATUS : {

                    doClean(user, callback, true, false, false);

                    return true;

                }

                case POINT_CLEAN_FOLLOWERS : {

                    doClean(user, callback, false, true, false);

                    return true;

                }

                case POINT_CLEAN_FRIDENDS : {

                    doClean(user, callback, false, false, true);

                    return true;

                }

        }

        return false;

    }

}
