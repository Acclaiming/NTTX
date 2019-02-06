package io.kurumi.nttools.twitter;

import io.kurumi.nttools.fragments.FragmentBase;
import io.kurumi.nttools.model.Msg;
import io.kurumi.nttools.utils.UserData;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class TwitterFunc extends FragmentBase {

    public static TwitterFunc INSTANCE = new TwitterFunc();
    
    @Override
    public boolean processPrivateMessage(UserData user, Msg msg) {

        if (!msg.isCommand()) return false;

        switch (msg.commandName()) {

                case "gtu" : gtu(user, msg);break;
                case "gts" : gts(user, msg);break;

                default : return false;

        }

        return true;

    }

    private static String[] noAccount = new String[] {

        "还没有认证Twitter账号 🤔",
        "这个功能使用的TwitterApi需要用户上下文 (",
        "使用 /newTwitterAuth 认证",

    };

    private static boolean noAccount(UserData userData) {

        return userData.twitterAccounts.size() == 0;

    }

    private static void gts(UserData user, Msg msg) {

        if (noAccount(user)) {

            msg.send(noAccount).exec();

            return;

        }

        String[] usage = new String[] {

            "无效的用法 ‼(•'╻'• ۶)۶","",
            "/gts [长整型] <StatusId> 例子 : 1145141919810"

        };

        if (msg.commandParms().length != 1) {

            msg.send(usage).exec();

            return;

        }

        try {

            long id = Long.parseLong(msg.commandParms()[0]);

            for (TwiAccount acc : user.twitterAccounts) {

                try {

                    Twitter api =  acc.createApi();

                    Status s = api.showStatus(id);

                    msg.send(printStatus(s));

                    return;

                } catch (TwitterException exc) {
                }

            }

            msg.send("没有这个推文或所有锁推/所有认证的账号被B无法取得");

        } catch ( Exception ex) {

            msg.send(usage).exec();

        }



    }

    private void gtu(UserData user, Msg msg) {

        if (user.twitterAccounts.size() == 0) {

            msg.send("没有认证Twitter账号 〒▽〒", "无法调用接口").exec();

            return;

        }


        if (noAccount(user)) {

            msg.send(noAccount).exec();

            return;

        }

        String[] usage = new String[] {

            "无效的用法 ‼(•'╻'• ۶)۶","",
            "/gtu [长整型] <AccountId> 例子 : 1145141919810",
            "/gtu [@开头的用户名] <ScreenName> 例子 : @HiedaNaKan",

        };

        if (msg.commandParms().length != 1) {

            msg.send(usage).exec();

            String idOrScreenName = msg.commandParms()[0];

            try {

                long id = Long.parseLong(idOrScreenName);

                try {

                    User u = user.twitterAccounts.getFirst().createApi().showUser(id);

                    msg.send(printUser(u)).exec();

                    return;


                } catch (Exception ecc) {

                    msg.send("没有那样的Twitter用户 : " + id).exec();

                    return;

                }



            } catch (Exception ex) {

                if (!idOrScreenName.startsWith("@")) {

                    msg.send(usage).exec();
                    return;

                }

                String screenName = idOrScreenName;

                try {

                    User u = user.twitterAccounts.getFirst().createApi().showUser(screenName.substring(1));

                    msg.send(printUser(u)).exec();

                } catch (Exception ecc) {

                    msg.send("没有那样的Twitter用户 : " + screenName).exec();

                }


            }

        }

    }


    private static String printStatus(Status s) {

        StringBuilder desc =  new StringBuilder("推文 : ")
            .append("ID : ").append(s.getId()).append("\n")

            .append("发送者 : [").append(TApi.formatUserName(s.getUser()))
            .append("](https://twitter.com/").append(s.getUser().getScreenName())
            .append("/").append(s.getId()).append(")").append("\n\n")

            .append("内容 : ").append("\n\n")
            .append("发送者信息 : ").append(printUser(s.getUser()));

        if (s.getQuotedStatus() != null) {

            desc.append("回复给 : \n\n").append(printStatus(s.getQuotedStatus()));

        }

        return desc.toString();


    }


    private static String printUser(User u) {

        return new StringBuilder("Twitter 用户 : ")

            .append("@").append(u.getScreenName()).append("\n")
            .append("用户名 : ").append(u.getName()).append("\n")
            .append("账号Id : ").append(u.getId()).append("\n\n")

            .append("简介 : ").append(u.getDescription()).append("\n\n")

            .append("关注者 : ").append(u.getFollowersCount())
            .append("正在关注 : ").append(u.getFriendsCount()).append("\n\n")

            .append("位置 : ").append(u.getLocation()).append("\n\n")
            .append("锁推 : ").append(u.isProtected() ? "是" : "否").append("\n\n")

            .append("创建的列表数量 : ").append(u.getListedCount()).append("\n\n")

            .append("注册时间 : ").append(u.getCreatedAt().toLocaleString()).append("\n\n")

            .append("主页地址 : ").append(u.getURL()).toString();


    }


}
