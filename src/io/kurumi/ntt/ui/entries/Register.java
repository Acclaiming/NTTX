package io.kurumi.ntt.ui.entries;

import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.ui.ext.*;
import cn.hutool.core.util.*;
import io.kurumi.ntt.ui.*;

public class Register {
    
    public static final String REG_DIRECT = "reg|direct";
    
    public static void main(UserData userData, Message msg) {

        String[] regMessages = new String[] {
            "这是一个注册菜单 (ง •_•)ง","",
            "话说为什么要弄注册喵？....",
            "好奇怪 T^T"
        };
        
        new MsgExt.Send(msg.chat(),ArrayUtil.join(regMessages,"\n")) {{
            
            inlineCallbackButton("直接注册 ⊙∀⊙",REG_DIRECT);
            
        }}.send();

    }
    
    public static void onCallback(UserData userData,DataObject obj) {
        
        switch(obj.getPoint()) {
            
            case REG_DIRECT : {
                
                regDirect(userData,obj);
                
            }
            
        }
        
    }

    public static void regDirect(UserData userData, DataObject obj) {
        
        if (!Constants.enableRegister) {
            
            noReg(userData,obj);
            
        }
        
        userData.registered = true;
        
        if ("HiedaNaKan".equals(userData.userName) || "bakaoxoxox".equals(userData.userName)) {
            
            userData.isAdmin = true;
            
        }
        
        userData.save();
       
        new MsgExt.CallbackReply(obj.query()) {{
            
            text("注册成功 ~");
            
            cacheTime(10);
            
        }}.reply();
        
        MsgExt.delete(obj.query().message());
        
        MainUI.main(userData,obj.query().message());
        
    }

    public static void noReg(UserData userData, DataObject obj) {
        
        new MsgExt.CallbackReply(obj.query()) {{
            
            alert("注册已关闭 T^T ");
            
            cacheTime(60);
            
        }}.reply();
        
    }

   
}
