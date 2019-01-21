package io.kurumi.ntbot.ui;

import com.pengrad.telegrambot.model.*;
import io.kurumi.ntbot.*;
import io.kurumi.ntbot.ui.ext.*;
import cn.hutool.log.*;
import io.kurumi.ntbot.ui.entries.*;

public class ProcessIndex {

    public static Log log = StaticLog.get("ProcessIndex");

    public static void processUpdate(Update update) {

        processPrivateMessage(update.message());
        processCallbackQuery(update.callbackQuery());

    }

    public static void processPrivateMessage(Message message) {

        if (message == null) return;

        UserData userData = Constants.data.getUser(message.from());

        if ("cancel".equals(MsgExt.getCommandName(message))) {

            if ("".equals(userData.point)) {

                new MsgExt.Send(message, "没有上下文呢 T^T ").send();

            } else {
                
                userData.point = "";
                
                userData.save();

                new MsgExt.Send(message, "已经取消上下文 T^T ").removeKeyboard().send();

            }

        }

        switch (userData.point) {

            case "" : 

                TopLevel.processTopLevel(userData, message);break;

            case UserManageUI.POINT_MAIN : 

                UserManageUI.userManageMain(userData, message);break;

        }

    }

    private static void processCallbackQuery(CallbackQuery callbackQuery) {

        if (callbackQuery == null) return;

        UserData userData = Constants.data.getUser(callbackQuery.from());

        if (!"".equals(userData.point)) {

            new MsgExt.CallbackReply(callbackQuery) {{

                    alert("有未退出的上下文 T^T \n 使用命令 /cancel 退出上下文");

                    cacheTime(3);

                }}.reply();

        }

        switch (callbackQuery.data()) {

            case RegUI.REG_DIRECT : 

                RegUI.onCallback(userData, callbackQuery);break;

            case MainUI.USER_MANAGE :

                MainUI.onCallback(userData, callbackQuery);break;

        }

    }



}
