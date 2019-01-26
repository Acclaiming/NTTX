package io.kurumi.ntt.ui.entries;

import io.kurumi.ntt.*;
import io.kurumi.ntt.ui.*;
import io.kurumi.ntt.ui.request.*;
import com.pengrad.telegrambot.request.*;

public class Admin {

    public static final String MAIN = "admins|main";

    public static final String STOP_BOT = "admin|exit";

    public static AbsResuest onCallback(UserData userData, DataObject obj) {

        switch (obj.getPoint()) {

                case MAIN : return main(userData, obj);

                case STOP_BOT : return stopBot(userData, obj);

        }

        return obj.reply().alert("没有那样的管理员指针 : " + obj.getPoint());

    }

    public static AbsResuest main(UserData userData, DataObject obj) {

        if (!userData.isAdmin) {

            return obj.reply().alert("u are not ADMIN！");

        }

        String[] adminMsg = new String[] {

            "乃就是咱Bot的Master吗！！！","",
            "( 其实是根据id判断的",
            "有一种钦定的感觉...",

        };

        return new EditMsg(obj.msg(), adminMsg) {{

                singleLineButton("停止BOT", STOP_BOT);

                singleLineButton("<< 返回主菜单", MainUI.BACK_TO_MAIN);

            }};


    }

    public static AbsResuest stopBot(UserData userData, DataObject obj) {

        if (!userData.isAdmin) {

            return obj.reply().alert("u are not ADMIN！");

        }

        BotControl.stopAll();

        if (Constants.authandwebhook.server != null) {

            Constants.authandwebhook.server.stop();
            
        }

        Constants.bot.removeGetUpdatesListener();
        
        Constants.bot.execute(new DeleteWebhook());

        new Thread(new Runnable() {

                @Override
                public void run() {

                    try {

                        Thread.sleep(10 * 1000);

                    } catch (InterruptedException e) {}

                    System.exit(0);

                }

            }).start();

        return obj.reply().alert("BOT已结束 正在结束进程...").cacheTime(10);

    }



}
