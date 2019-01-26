package io.kurumi.ntt.ui.entries;

import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.ui.ext.*;
import cn.hutool.core.util.*;
import io.kurumi.ntt.ui.*;
import io.kurumi.ntt.ui.request.*;

public class Register {

    public static final String REG_DIRECT = "reg|direct";

    public static AbsResuest main(UserData userData, Message msg) {

        String[] regMsg = new String[] {
            "这是一个注册菜单 (ง •_•)ง","",
            "话说为什么要弄注册喵？....",
            "好奇怪 T^T"
        };

        return new SendMsg(msg.chat(), regMsg) {{

                singleLineButton("直接注册 ⊙∀⊙", REG_DIRECT);

            }};

    }

    public static AbsResuest onCallback(UserData userData, DataObject obj) {

        switch (obj.getPoint()) {

                case REG_DIRECT : {

                    return regDirect(userData, obj);

                }

        }
        
        return obj.reply().alert("没有那样的注册指针 : " + obj.getPoint());

    }

    public static AbsResuest regDirect(final UserData userData, DataObject obj) {

        if (!Constants.enableRegister) {

            return noReg(userData, obj);

        }

        userData.registered = true;

        if ("HiedaNaKan".equals(userData.userName) || "bakaoxoxox".equals(userData.userName)) {

            userData.isAdmin = true;

        }

        userData.save();

        obj.deleteMsg();

        MainUI.sendMain(userData, obj.msg(), false);
        
        return new AnswerCallback(obj.query()) {{

                if (userData.isAdmin) {

                    alert("行政员账号注册成功 ( for you only ！");

                } else {

                    text("注册成功 ~");

                }

                cacheTime(10);

            }};


    }

    public static AbsResuest noReg(UserData userData, DataObject obj) {

        return obj.reply().alert("注册已关闭 T^T ").cacheTime(60);
        
    }


}
