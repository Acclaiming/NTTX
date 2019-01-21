package io.kurumi.ntbot.ui.entries;

import io.kurumi.ntbot.*;
import com.pengrad.telegrambot.model.*;
import io.kurumi.ntbot.ui.ext.*;
import io.kurumi.ntbot.twitter.*;
import java.util.*;
import twitter4j.*;

public class UserManageUI {

    public static final String COMMAND = "users";
    
    public static final String POINT_MAIN = "users|main";
    
    public static final String[] userManageMessages = new String[] {

        "这里是Twitter账号管理菜单 ~","",
        "使用 新建 来认证新账号 ！",
        "点击账号以更改 、 删除 (‵▽′)/","",
        "或者使用 /cancel 退出 +_+"

    };
    
    public static void main(final UserData userData, Message msg) {

        
        
        new MsgExt.Send(msg.chat(), userManageMessages) {{

                keyBoardButton("添加账号");
                
                for (TwiAccount account : userData.twitterAccounts) {

                    keyBoardButton(account.getFormatedName());

                }

            }}.send();

    }
    
  //  public static void

    public static void startOAuth(UserData userData, CallbackQuery query) {
        
        

    }

}
