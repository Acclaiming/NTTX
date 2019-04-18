package io.kurumi.ntt.funcs;

import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.utils.*;

public class Utils extends Fragment {

    public static Utils INSTANCE = new Utils();
    
    @Override
    public boolean onMsg(UserData user,Msg msg) {

        if (!msg.isCommand()) return false;

        switch (msg.command()) {

            case "stu" : showTgUser(user,msg);break;

            default : return false;

        }

        return true;

    }

    void showTgUser(UserData user,Msg msg) {

        msg.send(Html.a("目标用户","tg://user?id=" + msg.params()[0])).html().exec();

    }

}
