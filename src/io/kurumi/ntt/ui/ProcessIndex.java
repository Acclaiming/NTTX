package io.kurumi.ntt.ui;

import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.ui.ext.*;
import cn.hutool.log.*;
import io.kurumi.ntt.ui.entries.*;
import io.kurumi.ntt.ui.request.*;
import io.kurumi.ntt.ui.funcs.*;

public class ProcessIndex {

    public static Log log = StaticLog.get("ProcessIndex");

    public static void processUpdate(Update update) {

        if (update.message() != null) {

            switch (update.message().chat().type()) {

                    case supergroup : 
                    case group : processGroupMessage(update.message());return;
                    case Private :processPrivateMessage(update.message());return; 

            }

        }

        processCallbackQuery(update.callbackQuery());

    }

    public static void processGroupMessage(Message message) {

        if (message == null) return;
        if (message.text() == null) return;

        UserData userData = Constants.data.getUser(message);

        // funcs

        SeeYouNextTime.processGrpupMessage(userData, message);

        if (MsgExt.isCommand(message)) {

            switch (MsgExt.getCommandName(message)) {}

        }

    }

    public static void processPrivateMessage(Message message) {

        if (message == null) return;
        if (message.text() == null) return;

        UserData userData = Constants.data.getUser(message);

        if (userData.isBanned) {

            new SendMsg(message, "you are **banned**").markdown().exec();

            return;

        }

        if ("cancel".equals(MsgExt.getCommandName(message))) {

            if (userData.point == null) {

                new SendMsg(message, "没有上下文呢 T^T ").exec();

            } else {

                userData.point = null;
                userData.save();

                new SendMsg(message, "已经取消上下文 T^T ").removeKeyboard().exec();

            }

            return;

        }


        switch (userData.getPoint()) {

                case "" : 

                TopLevel.processTopLevel(userData, message);break;

                case Account.POINT_INPUT_AUTH_URL : 

                Account.onInputUrl(userData, message);break;

        }

    }

    private static void processCallbackQuery(CallbackQuery callbackQuery) {

        if (callbackQuery == null) return;

        UserData userData = Constants.data.getUser(callbackQuery.from());

        if (userData.point != null) {

            new AnswerCallback(callbackQuery).alert("有未退出的上下文 T^T \n 使用命令 /cancel 退出上下文").cacheTime(3).exec();

            return;

        }

        DataObject obj = new DataObject(callbackQuery);

        switch (obj.getPoint()) {

                case Register.REG_DIRECT : 

                Register.onCallback(userData, obj);
                return;

                case MainUI.BACK_TO_MAIN :

                MainUI.onCallback(userData, obj);
                return;

                case Account.MAIN: 
                case Account.ADD_ACCOUNT :
                case Account.MANAGE_ACCOUNT :

                case Account.BACK_TO_USERLIST :

                case Account.DEL_ACCOUNT :
                case Account.CANCEL_DEL_ACCOUNT :
                case Account.CONFIRM_DEL_ACCOUNT :

                Account.onCallBack(userData, obj);
                return;

                case Admin.ADMIN_MAIN : 
                case Admin.STOP_BOT :

                Admin.onCallback(userData, obj);
                return;

        }

        obj.reply().alert("Error : 没有那样的函数指针入口 " + obj.getPoint()).exec();


    }



}
