package io.kurumi.ntt.ui.entries;

import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.ui.ext.*;
import io.kurumi.ntt.md.*;
import io.kurumi.ntt.ui.request.*;
import cn.hutool.log.*;

public class TopLevel {

    public static final String COMMAND_START = "start";

    public static AbsResuest processTopLevel(UserData userData, Message msg) {

        if (MsgExt.isCommand(msg)) {

            switch (MsgExt.getCommandName(msg)) {

                case COMMAND_START : return start(userData, msg);
                case MainUI.COMMAND : return MainUI.main(userData, msg);
                
                
            }

        }

        //  repeat(userData, msg);

      
        return new SendMsg(msg.chat(),"嘤");
        
    }
    
    public static String[] startMessage = new String[] {

        "你好呀！ 这里是NTTBot ！ (不是复读机！)","",

        "Bot的源码在这里 ！ [NTTools](https://github.com/HiedaNaKan/NTTools)",
        "可以的话能给个star吗 (◦˙▽˙◦)","",

        "咱的推特在 [NTTBot](https://twitter.com/NTToolsBot)",
        "这里！欢迎关注哦 ⊙∀⊙ 咱会在十分钟之内回关呢 ←_←","",

        "现在 用 /main 来打开主菜单哦 >_<" 

    };

    public static AbsResuest start(UserData userData, Message msg) {

        String[] sendMsg = Markdown.encode(startMessage);
        
        sendMsg[0] = userData.name + " " + sendMsg[0];

        return new SendMsg(msg.chat(), sendMsg).markdown().disableWebPagePreview();

    }

    public static AbsResuest repeat(UserData userData, Message msg) {

        return new SendMsg(msg, msg.text());

    }


}
