package io.kurumi.ntt.ui.entries;

import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.ui.ext.*;
import io.kurumi.ntt.ui.request.*;
import io.kurumi.ntt.twitter.*;
import twitter4j.User;

public class Functions {
    
    public static final String FUNC_GET_TWITTER_USER = "getTwitterUser";

    public static AbsResuest main(UserData userData, Message msg) {

        String name = MsgExt.getCommandName(msg);
        String[] params = MsgExt.getCommandParms(msg);

        switch (name) {

                case FUNC_GET_TWITTER_USER : getTwitterUser(userData , params, msg);

        }
        
        return null;

    }

    private static String[] noAccount = new String[] {

        "还没有认证Twitter账号 🤔",
        "这个功能使用的TwitterApi需要用户上下文 (",
        "使用 /newTwitterAuth 认证",

    };

    private static boolean noAccount(UserData userData) {

        return userData.twitterAccounts.size() == 0;

    }

    private static AbsResuest getTwitterUser(UserData userData , String[] params , Message msg) {

        if (noAccount(userData)) return new SendMsg(msg.chat() , noAccount);

        String[] usage = new String[] {

            "无效的用法 ‼(•'╻'• ۶)۶","",
            "/getTwitterUser [长整型] <AccountId> 例子 : 1145141919810",
            "/getTwitterUser [@开头的用户名] <ScreenName> 例子 : @HiedaNaKan",

        };

        if (params.length != 1) return new SendMsg(msg.chat(), usage);

        String idOrScreenName = params[0];

        try {

            long id = Long.parseLong(idOrScreenName);

            try {

                User u = userData.twitterAccounts.getFirst().createApi().showUser(id);

                return new SendMsg(msg.chat(), printUser(u));
                
                
            } catch (Exception ecc) {

                return new SendMsg(msg.chat(), "没有那样的Twitter用户 : " + id);

            }



        } catch (Exception ex) {

            if (!idOrScreenName.startsWith("@")) {

                return new SendMsg(msg, usage);

            }

            String screenName = idOrScreenName;
            
            try {

                User u = userData.twitterAccounts.getFirst().createApi().showUser(screenName.substring(1));

                return new SendMsg(msg.chat(), printUser(u));

            } catch (Exception ecc) {

                return new SendMsg(msg.chat(), "没有那样的Twitter用户 : " + screenName);

            }
            

        }

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
