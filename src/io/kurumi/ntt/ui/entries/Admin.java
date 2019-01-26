package io.kurumi.ntt.ui.entries;

import io.kurumi.ntt.*;
import io.kurumi.ntt.ui.*;
import io.kurumi.ntt.ui.request.*;

public class Admin {

    public static final String ADMIN_MAIN = "admins|main";

    public static final String STOP_BOT = "admin|exit";

    public static void onCallback(UserData userData, DataObject obj) {

        switch (obj.getPoint()) {

                case ADMIN_MAIN : main(userData, obj);return;
                case STOP_BOT : stopBot(userData, obj);

        }

    }

    public static void main(UserData userData, DataObject obj) {

        if (!userData.isAdmin) {

            obj.reply().alert("u are not ADMIN！").exec();

            return;

        }

        new EditMsg(obj.msg()) {{

                singleLineButton("停止BOT", STOP_BOT);
                
                singleLineButton("<< 返回主菜单",MainUI.BACK_TO_MAIN);

            }}.exec();


    }

    public static void stopBot(UserData userData, DataObject obj) {

        if (!userData.isAdmin) {

            obj.reply().alert("u are not ADMIN！").exec();

            return;

        }

        obj.reply().alert("正在停止BOT").cacheTime(3).exec();

        Constants.auth.server.stop();

        Constants.bot.removeGetUpdatesListener();

        System.exit(0);

    }



}
