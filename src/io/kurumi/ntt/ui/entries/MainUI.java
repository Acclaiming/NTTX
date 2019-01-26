package io.kurumi.ntt.ui.entries;

import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.ui.ext.*;
import io.kurumi.ntt.ui.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.auth.*;
import io.kurumi.ntt.ui.request.*;

public class MainUI {

    public static final String COMMAND = "main";

    public static final String USER_MANAGE = "main|user_manage";
    public static final String BACK_TO_MAIN = "main|back";

    public static String[] mainMessages = new String[] {

        "你好呢... 这里是咱的菜单呢 (◦˙▽˙◦)","",
        "有点简单... 不过乃可以点 「建议」 跟咱说哦！ (≧▽≦)"

    };

    public static void main(UserData userData, Message msg) {

        if (!userData.registered) {

            Register.main(userData, msg);

        } else {

            sendMain(userData,msg,false);
            
        }

    }

    public static void sendMain(UserData userData, Message msg , boolean edit) {
       
        AbsSendMsg send;
        
        if (edit) send = new EditMsg(msg, mainMessages);
        else send = new SendMsg(msg.chat(),mainMessages);
        
    
                send.singleLineButton("管理已认证的账号 (‵▽′)", USER_MANAGE);

               send.singleLineOpenUrlButton("给咱建议！ 「新功能/报错...」", "https://t.me/HiedaNaKan");

         send.exec();
        
            
    }

    public static void processPoint(UserData userData, Message msg) {

        switch (userData.point) {

                case Account.POINT_INPUT_AUTH_URL : Account.onInputUrl(userData, msg);return;

        }

    }

    public static void onCallback(UserData userData, DataObject obj) {

        switch (obj.getPoint()) {

                case BACK_TO_MAIN :

                sendMain(userData, obj.msg(),true);
                obj.confirmQuery();

                return;


                case USER_MANAGE : 
                Account.changeTo(userData, obj);
                obj.confirmQuery();
                return;

        }

    }





}
