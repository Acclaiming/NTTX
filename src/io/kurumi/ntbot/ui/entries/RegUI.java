package io.kurumi.ntbot.ui.entries;

import com.pengrad.telegrambot.model.*;
import io.kurumi.ntbot.*;
import io.kurumi.ntbot.ui.ext.*;
import cn.hutool.core.util.*;

public class RegUI {
    
    public static void main(UserData userData, Message msg) {

        String[] regMessages = new String[] {
            "这是一个注册菜单 (ง •_•)ง","",
            "话说为什么要弄注册？...."
        };
        
        new MsgExt.Send(msg.chat(),ArrayUtil.join(regMessages,"\n")) {{
            
            inlineCallbackButton("直接注册","/");
            
        }}.send();

    }
    
}
