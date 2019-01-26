package io.kurumi.ntt.ui;

import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.ui.ext.*;
import cn.hutool.log.*;
import io.kurumi.ntt.ui.entries.*;

public class ProcessIndex {

    public static Log log = StaticLog.get("ProcessIndex");

    public static void processUpdate(Update update) {

        processPrivateMessage(update.message());
        processCallbackQuery(update.callbackQuery());

    }

    public static void processPrivateMessage(Message message) {

        if (message == null) return;
        if (message.text() == null) return;

        UserData userData = Constants.data.getUser(message);

        if ("cancel".equals(MsgExt.getCommandName(message))) {

            if ("".equals(userData.point)) {

                new MsgExt.Send(message, "没有上下文呢 T^T ").send();

            } else {

                userData.point = "";
                userData.save();

                new MsgExt.Send(message, "已经取消上下文 T^T ").removeKeyboard().send();

            }

            return;

        }


        switch (userData.point) {

                case "" : 

                TopLevel.processTopLevel(userData, message);break;

                case Account.POINT_INPUT_AUTH_URL : 

                Account.onInputUrl(userData, message);break;

        }

    }

    private static void processCallbackQuery(CallbackQuery callbackQuery) {

        if (callbackQuery == null) return;

        UserData userData = Constants.data.getUser(callbackQuery.from());
        
        if (!"".equals(userData.point)) {

            new MsgExt.CallbackReply(callbackQuery).alert("有未退出的上下文 T^T \n 使用命令 /cancel 退出上下文").cacheTime(3).reply();

            return;

        }

        DataObject obj = new DataObject(callbackQuery);

        switch (obj.getPoint()) {

                case Register.REG_DIRECT : 

                Register.onCallback(userData, obj);break;

                case MainUI.USER_MANAGE :
                case MainUI.BACK_TO_MAIN :

                MainUI.onCallback(userData, obj);break;

                case Account.ADD_ACCOUNT :
                case Account.MANAGE_ACCOUNT :

                case Account.BACK_TO_USERLIST :

                case Account.DEL_ACCOUNT :
                case Account.CANCEL_DEL_ACCOUNT :
                case Account.CONFIRM_DEL_ACCOUNT :

                Account.onCallBack(userData, obj);break;

        }

        obj.reply().alert("Error : " + obj.getPoint()).reply();


    }



}
