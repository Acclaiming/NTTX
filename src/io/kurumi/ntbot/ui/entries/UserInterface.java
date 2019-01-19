package io.kurumi.ntbot.ui.entries;

import io.kurumi.ntbot.*;
import com.pengrad.telegrambot.model.*;
import io.kurumi.ntbot.ui.ext.*;
import com.pengrad.telegrambot.model.request.*;
import io.kurumi.ntbot.ui.callback.*;
import cn.hutool.log.*;

public class UserInterface {

    public static final int index_none = 0;

    public Log log;
    
    public UserData userData;

    public UserInterface(UserData userData) {

        this.userData = userData;
        
        log = StaticLog.get("UserInterface");

    }


    public int getIndex() {

        return userData.getInt("index", index_none);

    }

    public void setIndex(int index) {

        userData.set("index", index);
        userData.save();

    }

    public int getIndexX(int x) {

        return userData.getInt("index" + x, index_none);

    }

    public void setIndexX(int x, int index) {

        userData.set("index_" + x, index);
        userData.save();

    }

    public void processMessage(Message msg) {

        switch (getIndex()) {

            case index_none : processTopLevel(msg);

        }

    }

    private void processTopLevel(Message msg) {

        if (MsgExt.isCommand(msg)) {

            switch (MsgExt.getCommandName(msg)) {

                case "start" : onStart(msg);break;
                case "main" : onMain(msg);break;

            }

        }

    }

    public void onStart(Message msg) {

        new MsgExt.Send(msg, "/main").send();

    }

    public static final String CB_REG_DIRECT = "0|0|0";
    public static final String CB_REG_USING_CODE = "0|0|1";
    public static final String CB_API_MANAGE = "0|1|0";
    public static final String CB_ACCOUNT_MANAGE = "0|1|1";
    public static final String CB_ADMIN_MENU = "0|1|2";


    public void onMain(Message msg) {

        log.debug("/main");
        
        if (msg.from().equals(Constants.thisUser)) {

            log.debug("/main when return");
            
            MsgExt.Edit edit = new MsgExt.Edit(msg,"NTTBot 一个简陋的菜单 ~")
                .inlineCallbackButton("API管理", CB_API_MANAGE)
                .inlineCallbackButton("账号管理", CB_ACCOUNT_MANAGE);

            if (userData.isAdmin()) {
                edit.inlineCallbackButton("管理菜单", CB_ADMIN_MENU);
            }

            edit.edit();

        } else if (userData.isRegistered()) {

            MsgExt.Send send = new MsgExt.Send(msg.chat(), "NTTBot 一个简陋的菜单 ~")
                .inlineCallbackButton("API管理", CB_API_MANAGE)
                .inlineCallbackButton("账号管理", CB_ACCOUNT_MANAGE);

            if (userData.isAdmin()) {
                send.inlineCallbackButton("管理菜单", CB_ADMIN_MENU);
            }

            send.send();


        } else {

            new MsgExt.Send(msg.chat(), "NTTBot v " + BotMain.version)
                .inlineCallbackButton("直接注册", CB_REG_DIRECT)
            //        .inlineCallbackButton("使用邀请码注册", CB_REG_USING_CODE)
            .send();

        }

    }

    public void processCallback(String data, CallbackQuery callbackQuery) {

        switch (data) {

            case CB_REG_DIRECT : regDirect(callbackQuery);break;
                //     case CB_REG_USING_CODE : regUsingCode();break;

            case CB_API_MANAGE : apiManage(callbackQuery);

        }

    }

    private void regDirect(CallbackQuery callbackQuery) {

        if (Constants.needInvitationCode) {

            new MsgExt.Send(callbackQuery.message().chat(),
                            "暂时不能直接注册呢 ~");

        } else {

            if (userData.getUserName().equals("HiedaNaKan")) {

                userData.setAdmin(true);

            }

            userData.setRegistered(true);
            userData.save();

            new MsgExt.Send(callbackQuery.message().chat(),
                            "注册成功 ~").send();

            onMain(callbackQuery.message());

        }

    }

    private void apiManage(CallbackQuery callbackQuery) {



    }


}
