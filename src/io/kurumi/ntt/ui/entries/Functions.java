package io.kurumi.ntt.ui.entries;

import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.ui.ext.*;
import io.kurumi.ntt.ui.request.*;
import io.kurumi.ntt.twitter.*;
import twitter4j.User;
import twitter4j.*;

public class Functions {

    public static final String FUNC_GET_TWITTER_USER = "gtu";
    public static final String FUNC_GET_TWITTER_STATUS = "gts";

    public static AbsResuest main(UserData userData, Message msg) {

        String name = MsgExt.getCommandName(msg);
        String[] params = MsgExt.getCommandParms(msg);
        
       

        switch (name) {

                case FUNC_GET_TWITTER_USER : return getTwitterUser(userData , params, msg);
                case FUNC_GET_TWITTER_STATUS : return getTwitterStatus(userData, params, msg);

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

    private static AbsResuest getTwitterStatus(UserData userData , String[] params , Message msg) {

        if (noAccount(userData)) return new SendMsg(msg.chat() , noAccount);

        String[] usage = new String[] {

            "无效的用法 ‼(•'╻'• ۶)۶","",
            "/gts [长整型] <StatusId> 例子 : 1145141919810"

        };

        if (params.length != 1) return new SendMsg(msg.chat(), usage);

        try {

            long id = Long.parseLong(params[0]);
            
            for (TwiAccount acc : userData.twitterAccounts) {

                try {

                    Twitter api =  acc.createApi();

                    Status s = api.showStatus(id);
                    
                    return new SendMsg(msg.chat(), printStatus(s));

                } catch (TwitterException exc) {
                    
                    
                    
                }

            }
            
            return new SendMsg(msg.chat(), "没有这个推文或所有锁推/所有认证的账号被B无法取得");

          } catch( Exception ex) {
              
              return new SendMsg(msg.chat(), usage);
              
          }

        

    };


    private static AbsResuest getTwitterUser(UserData userData , String[] params , Message msg) {

        if (noAccount(userData)) return new SendMsg(msg.chat() , noAccount);

        String[] usage = new String[] {

            "无效的用法 ‼(•'╻'• ۶)۶","",
            "/gtu [长整型] <AccountId> 例子 : 1145141919810",
            "/gtu [@开头的用户名] <ScreenName> 例子 : @HiedaNaKan",

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
