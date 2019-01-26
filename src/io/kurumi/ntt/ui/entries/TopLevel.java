package io.kurumi.ntt.ui.entries;

import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.ui.ext.*;
import io.kurumi.ntt.md.*;
import io.kurumi.ntt.ui.request.*;

public class TopLevel {

    public static final String COMMAND_START = "start";

    public static void processTopLevel(UserData userData, Message msg) {

        if (MsgExt.isCommand(msg)) {

            switch (MsgExt.getCommandName(msg)) {

                case COMMAND_START : start(userData, msg);return;
                case MainUI.COMMAND : MainUI.main(userData, msg);return;
                
                
            }

        }

        //  repeat(userData, msg);

      
        new SendMsg(msg.chat(),"嘤").exec();
        
    }
    
    public static String[] startMessage = new String[] {

        "你好呀！ 这里是NTTBot ！ (不是复读机！)","",

        "Bot的源码在这里 ！ [NTTools](https://github.com/HiedaNaKan/NTTools)",
        "可以的话能给个star吗 (◦˙▽˙◦)","",

        "咱的推特在 [NTTBot](https://twitter.com/NTToolsBot)",
        "这里！欢迎关注哦 ⊙∀⊙ 咱会在十分钟之内回关呢 ←_←","",

        "现在 用 /main 来打开主菜单哦 >_<" 

    };

    public static void start(UserData userData, Message msg) {

        String[] sendMsg = Markdown.encode(startMessage);
        
        sendMsg[0] = userData.name + " " + sendMsg[0];

        new SendMsg(msg.chat(), sendMsg).markdown().disableWebPagePreview().exec();

    }

    public static void repeat(UserData userData, Message msg) {

        new SendMsg(msg, msg.text()).exec();

    }


}
