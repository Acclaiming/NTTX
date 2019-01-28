package io.kurumi.ntt.ui;

import cn.hutool.log.*;
import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.ui.entries.*;
import io.kurumi.ntt.ui.ext.*;
import io.kurumi.ntt.ui.request.*;
import io.kurumi.ntt.ui.confs.*;

public class ProcessIndex {

    public static Log log = StaticLog.get(ProcessIndex.class);

    public static AbsResuest processUpdate(Update update) {

        try {

            if (update.message() != null) {

                switch (update.message().chat().type()) {

                     //   case supergroup : 
                     //   case group : return processGroupMessage(update.message());
                        case Private : return processPrivateMessage(update.message());

                }

            } else if (update.callbackQuery() != null) {

                return processCallbackQuery(update.callbackQuery());

            }

        } catch (Exception e) {

            log.error(e, "处理更新失败");

            for (UserData userData : Constants.data.getUsers()) {

                if (userData.isAdmin && userData.chat != null) {

                    StringBuilder err = new StringBuilder();

                    err.append("Bot出错 : ");

                    err.append("\n更新 : " + update);

                    Throwable cause = e;

                    while (cause != null) {

                        err.append("\n\n错误 : " + cause.getClass().getName());

                        err.append("\n\n" + cause.getMessage());

                        for (StackTraceElement stack : cause.getStackTrace())  {

                            err.append("\nat : " + stack.toString());
                        }

                        cause = cause.getCause();

                    }

                    new SendMsg(userData.chat, err.toString()).exec();

                }

                System.exit(1);

            }

        }

        return null;

    }


    public static AbsResuest processGroupMessage(Message message) {

        if (message == null) return null;
        if (message.text() == null) return null;

        UserData userData = Constants.data.getUser(message);

        return null;
    }

    public static AbsResuest processPrivateMessage(Message message) {

        if (message == null) return null;
        if (message.text() == null) return null;

        UserData userData = Constants.data.getUser(message);

        if (userData.isBanned) {

            return new SendMsg(message, "you are **banned**").markdown();

        }

        if ("cancel".equals(MsgExt.getCommandName(message))) {

            if (userData.point == null) {

                return new SendMsg(message, "没有上下文呢 T^T ");

            } else {

                userData.point = null;
                userData.save();

                return new SendMsg(message, "已经取消上下文 T^T ").removeKeyboard();

            }

        }


        switch (userData.getPoint()) {

                case "" : 

                return TopLevel.processTopLevel(userData, message);

                case Account.POINT_INPUT_AUTH_URL : 

                return Account.onInputUrl(userData, message);

                case Bots.POINT_INPUT_NAME : 
                    
                return Bots.onInputName(userData,message);
                
                case BaseConf.POINT_CONF_INPUT :
                
                return Bots.onConfInput(userData,message);
                
        }

        return new SendMsg(message, "非法上下文 : " + userData.getPoint());

    }

    private static AbsResuest processCallbackQuery(CallbackQuery callbackQuery) {

        if (callbackQuery == null) return null;

        UserData userData = Constants.data.getUser(callbackQuery.from());

        if (userData.point != null) {

            return new AnswerCallback(callbackQuery).alert("有未退出的上下文 T^T \n 使用命令 /cancel 退出上下文").cacheTime(3);

        }

        DataObject obj = new DataObject(callbackQuery);

        switch (obj.getPoint()) {

                case Register.REG_DIRECT : 

                return Register.onCallback(userData, obj);

                case MainUI.BACK_TO_MAIN :

                return MainUI.onCallback(userData, obj);


                case Account.MAIN: 
                case Account.ADD_ACCOUNT :
                case Account.MANAGE_ACCOUNT :

                case Account.BACK_TO_USERLIST :

                case Account.DEL_ACCOUNT :
                case Account.CANCEL_DEL_ACCOUNT :
                case Account.CONFIRM_DEL_ACCOUNT :

                return Account.onCallBack(userData, obj);
                
                case Bots.MAIN :
                case Bots.NEW_BOT : 
                case Bots.INPUT_NAME : 
                case Bots.MANAGE_BOT :
                case BaseConf.CONF_BACK : 
                case BaseConf.CONF_CALLBACK :
                    
                return Bots.onCallback(userData,obj);
                 

                case Admin.MAIN : 
        //        case Admin.STOP_BOT :

                return Admin.onCallback(userData, obj);

        }

        return obj.reply().alert("Error : 没有那样的函数指针入口 " + obj.getPoint());


    }



}
