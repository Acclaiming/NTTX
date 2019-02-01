package io.kurumi.nttools.twitter;

import io.kurumi.nttools.model.Msg;
import io.kurumi.nttools.server.AuthCache;
import io.kurumi.nttools.twitter.ApiToken;
import io.kurumi.nttools.twitter.TwiAccount;
import io.kurumi.nttools.utils.UserData;
import java.util.LinkedList;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Option;
import io.kurumi.nttools.utils.CommandUI;
import org.apache.commons.cli.CommandLine;
import cn.hutool.core.util.ArrayUtil;

public class TwitterUI extends CommandUI {

    public static TwitterUI INSTANCE = new TwitterUI();

    public static final String FUNC = "twitter";

    @Override
    protected String cmdLineSyntax() {

        return "twitter";

    }

    @Override
    protected void applyOptions(UserData user, Options options) {

        options.addOption("a", "auth", false, "认证新Twitter账号");

        options.addOption("l", "list", false, "查看所有账号");
        
        options.addOption(Option.builder("r")
                          .longOpt("refresh")
                          .desc("刷新Twitter账号")
                          .hasArg()
                          .argName("账号/指针")
                          .build());

        options.addOption(Option.builder("d")
                          .longOpt("delete")
                          .desc("移除Twitter账号")
                          .hasArg()
                          .argName("账号/指针")
                          .build());

    }

    @Override
    protected void onCommand(final UserData user, Msg msg, CommandLine cmd) {

        if (cmd.hasOption("auth")) {

            final Msg status = msg.send("正在请求认证链接 (｡>∀<｡)").send();

            try {

                final RequestToken requestToken = ApiToken.defaultToken.createApi().getOAuthRequestToken("https://" + msg.fragment.main.serverDomain + "/callback");

                AuthCache.cache.put(requestToken.getToken(), new AuthCache.Listener() {

                        @Override
                        public void onAuth(String oauthVerifier) {

                            System.out.println("authing");

                            try {

                                AccessToken accessToken =  ApiToken.defaultToken.createApi().getOAuthAccessToken(requestToken, oauthVerifier);

                                TwiAccount account = new TwiAccount(ApiToken.defaultToken.apiToken, ApiToken.defaultToken.apiSecToken, accessToken.getToken(), accessToken.getTokenSecret());

                                account.refresh();

                                LinkedList<TwiAccount> acc = user.getTwitterAccounts();

                                acc.add(account);

                                user.setTwitterAccounts(acc);

                                user.save();

                                status.edit("认证成功 (｡>∀<｡) 乃的账号", account.getFormatedName()).markdown().exec();

                            } catch (Exception e) {

                                status.edit(e.toString()).exec();

                            }

                        }

                    });

                status.edit("请求成功 ╰(*´︶`*)╯\n 点这里认证 : ", requestToken.getAuthenticationURL()).exec();



            } catch (TwitterException e) {

                status.edit(e.toString()).exec();

            }

            return;

        }

        LinkedList<TwiAccount> accounts = user.getTwitterAccounts();

        if (accounts.isEmpty()) {

            msg.send("你还没有认证账号 (ﾟ⊿ﾟ)ﾂ", "使用 /twitter -auth 认证哦 (｡>∀<｡)").exec();

            return;

        }

        String screenName = cmd.getOptionValue("r");


        TwiAccount account;

        try {

            long accountId = Long.parseLong(screenName);

            account = user.findUser(accountId);

            if (account == null || accounts.size() > accountId) {

                account =  accounts.get((int)accountId);

            }

        } catch (Exception ex) {

            account = user.findUser(screenName);

        }

        if (account == null) {

            msg.send("没有那样的Twitter账号 (ﾟ⊿ﾟ)ﾂ", "所有账号 : ", "", ArrayUtil.join(accounts.toArray(new TwiAccount[accounts.size()]), "\n")).exec();

            return;

        }

        if (cmd.hasOption("refresh")) {

            if (account.refresh()) {

                msg.send("刷新成功 (｡>∀<｡)", account.getMarkdowName()).html().exec();

            } else {

                if (cmd.hasOption("remove")) {

                    accounts.remove(account);

                    user.setTwitterAccounts(accounts);

                    user.save();

                    msg.send("账号不可用 已移除 >_<").exec();

                } else {

                    msg.send("账号不可用 >_<").exec();

                }

            }


        } else if (cmd.hasOption("delete")) {

            accounts.remove(account);

            user.setTwitterAccounts(accounts);

            user.save();

            msg.send("已移除 >_<").exec();

        }



    }


}
