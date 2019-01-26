package io.kurumi.ntt.ui.entries;

import cn.hutool.log.*;
import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.ui.*;
import io.kurumi.ntt.ui.request.*;

public class MainUI {

    public static final String COMMAND = "main";

    public static final String BACK_TO_MAIN = "main|back";

    public static String[] mainMessages = new String[] {

        "你好呢... 这里是咱的菜单呢 (◦˙▽˙◦)","",
        "有点简单... 不过乃可以点 「建议」 跟咱说哦！ (≧▽≦)"

    };

    public static AbsResuest main(UserData userData, Message msg) {

        StaticLog.debug("main");

        if (!userData.registered) {

            return Register.main(userData, msg);

        } else {

            return sendMain(userData, msg, false);

        }

    }

    public static AbsResuest sendMain(UserData userData, Message msg , boolean edit) {

        AbsSendMsg send;

        if (edit) send = new EditMsg(msg, mainMessages);
        else send = new SendMsg(msg.chat(), mainMessages);


        send.singleLineButton("管理账号 (‵▽′)", Account.MAIN);

        if (userData.isAdmin) {

            send.singleLineButton("管理员菜单 *٩(๑´∀`๑)ง*", Admin.MAIN);

        }

        send.singleLineOpenUrlButton("给咱建议！ 「新功能/报错...」", "https://t.me/HiedaNaKan");

        return send;

    }

    public static void processPoint(UserData userData, Message msg) {

        switch (userData.getPoint()) {

                case Account.POINT_INPUT_AUTH_URL : Account.onInputUrl(userData, msg);return;

        }

    }

    public static AbsResuest onCallback(UserData userData, DataObject obj) {

        switch (obj.getPoint()) {

                case BACK_TO_MAIN :

                return sendMain(userData, obj.msg(), true);

        }

        return obj.reply().alert("无效的主页指针 : " + obj.getPoint());

    }





}
