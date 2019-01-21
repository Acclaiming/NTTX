package io.kurumi.ntbot.ui.entries;

import com.pengrad.telegrambot.model.*;
import io.kurumi.ntbot.*;
import io.kurumi.ntbot.ui.ext.*;

public class MainUI {

    public static final String COMMAND = "main";

    public static final String USER_MANAGE = "main|user_manage";

    public static String[] mainMessages = new String[] {

        "你好呢... 这里是咱的菜单呢 (◦˙▽˙◦)","",
        "有点简单... 不过乃可以点 「建议」 跟咱说哦！ (≧▽≦)"

    };

    public static void main(UserData userData, Message msg) {

        if (!userData.registered) {

            RegUI.main(userData, msg);

        } else {

            new MsgExt.Send(msg.chat(), mainMessages) {{

                    inlineCallbackButton("账号管理", USER_MANAGE);

                    inlineOpenUrlButton("建议", "https://t.me/HiedaNaKan");

                }}.send();

        }

    }

    public static void onRegistered(UserData userData, CallbackQuery query) {

        new MsgExt.Edit(query, mainMessages) {{

                inlineCallbackButton("账号管理", USER_MANAGE);

                inlineOpenUrlButton("建议", "https://t.me/HiedaNaKan");

            }}.edit();

    }

    public static void onCallback(UserData userData, CallbackQuery query) {

        switch (query.data()) {

            case USER_MANAGE : UserManageUI.main(userData, query.message());break;

        }

    }

}
