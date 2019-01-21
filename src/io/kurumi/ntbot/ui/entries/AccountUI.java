package io.kurumi.ntbot.ui.entries;

import io.kurumi.ntbot.*;
import com.pengrad.telegrambot.model.*;
import io.kurumi.ntbot.ui.ext.*;
import io.kurumi.ntbot.twitter.*;
import java.util.*;
import twitter4j.*;
import io.kurumi.ntbot.auth.*;
import cn.hutool.log.*;

public class AccountUI {

    public static Log log = StaticLog.get(AccountUI.class);
    
    public static final String COMMAND = "users";

    public static final String POINT_MAIN = "users|main";
    public static final String POINT_ADD = "users|add";

    public static final String[] userManageMessages = new String[] {

        "这里是Twitter账号管理菜单 ~","",
        "使用 新建 来认证新账号 ！",
        "点击账号以更改 、 删除 (‵▽′)/","",
        "或者使用 /cancel 退出 +_+"

    };

    public static void main(final UserData userData, Message msg) {

        userData.point = POINT_MAIN;
        userData.save();

        new MsgExt.Send(msg.chat(), userManageMessages) {{

                keyBoardButton("添加账号");

                for (TwiAccount account : userData.twitterAccounts) {

                    keyBoardButton(account.getFormatedName());

                }

            }}.send();

    }

    public static void processPoint(UserData userData, Message msg) {

        log.debug("onAccountPoint");
        
        switch(userData.point) {
            
            case POINT_MAIN : userManageMenu(userData,msg);break;
            case POINT_ADD : onInputUrl(userData,msg);break;
            
        }
        
        
    }
    
    private static void userManageMenu(UserData userData, Message msg) {
        
        if ("添加账号".equals(msg.text())) {

            addUser(userData, msg);

            return;

        } 

        for (TwiAccount account : userData.twitterAccounts) {

            if (account.getFormatedName().equals(msg.text())) {

                userManage(userData,msg,account);
                
                return;

            }

        }

        new MsgExt.Send(msg.chat(), "选一下... 或者使用 /cancel 退出").removeKeyboard().send();
        
    }

    public static void addUser(final UserData userData, final Message msg) {

        final String authUrl = Constants.auth.newRequest(new AuthListener() {

                @Override
                public void onAuth(TwiAccount account) {

                    userData.twitterAccounts.add(account);
                    userData.save();

                    new MsgExt.Send(msg.chat(), account.getFormatedName() + " 认证成功 ~").send();

                }

            });
        
        if (Constants.auth.server != null) {
            
            userData.point = "";
            userData.save();

            new MsgExt.Send(msg.chat(), "点击 「认证」 按钮来认证哦 (◦˙▽˙◦)") {{

                    inlineOpenUrlButton("认证", authUrl);

                }}.removeKeyboard().send();

        } else {
            
            userData.point = POINT_ADD;
            userData.save();
            
            new MsgExt.Send(msg.chat() , new String[] {
                
                "点击 「认证」 按钮来认证哦 (◦˙▽˙◦)","",
                "认证之后会跳转到一个本地 (127.0.0.1) 网页",
                "复制链接发给咱就行了呢...","",
                "注意 : 只能认证一次哦.. 注意复制链接地址 〒▽〒"
                
            }) {{

                    inlineOpenUrlButton("认证", authUrl);

                }}.removeKeyboard().send();
            
        }

    }
    
    public static void onInputUrl(UserData userData, Message msg) {
       
        TwiAccount account = Constants.auth.authByUrl(msg.text());

        if (account == null) {
            
            new MsgExt.Send(msg.chat(),"链接无效 ( •̥́ ˍ •̀ू ) 输入 /cancel 以取消认证..").send();
            return;
            
        }
        
        userData.point = "";
        
        userData.save();
        
    }
    
    
    public static void userManage(UserData userData, Message msg, TwiAccount account) {
        
        
        
    }

}
