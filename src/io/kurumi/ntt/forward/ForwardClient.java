package io.kurumi.ntt.forward;

import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.model.request.*;
import java.util.*;

public class ForwardClient extends BotFragment {

    public Long userId;
    public String botToken;

    public UserData user;

    public ForwardClient(Long userId,String botToken)  {
        this.userId = userId;
        this.botToken = botToken;

        this.user = BotDB.getUserData(userId);
    }

    @Override
    public String botName() {
        return "ForwardBotClient";
    }

    @Override
    public String getToken() {
        return botToken;
    }

    @Override
    public boolean onPPM(UserData user,Msg msg) {
        return onNPM(user,msg);
    }

    @Override
    public boolean onNPM(UserData user,Msg msg) {

        if ("start".equals(msg.command())) {

            msg.send("这里是 " + user.name() + " 的私聊BOT ✧٩(ˊωˋ*)و✧ 发送信息给咱就可以了 ~").html().exec();

        } else {
            
            msg.send("来自 " + user.userName() + " : ",new Date(msg.message().forwardDate()).toLocaleString()).exec();
            msg.forwardTo(userId);

        }

        return true;
    }

}
