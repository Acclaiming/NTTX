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

    public ForwardClient(UserData user,String botToken)  {
        this.userId = user.id;
        this.botToken = botToken;

        this.user = user;
    }

    @Override
    public String botName() {
        return "ForwardBotClient For " + user.name();
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

            msg.send("这里是 " + this.user.name() + " 的私聊BOT ✧٩(ˊωˋ*)و✧ 发送信息给咱就可以了 ~").html().exec();

        } else {
            
            new Send(this,userId,"来自 " + user.userName() + " : ",new Date(msg.message().forwardDate() == null ? msg.message().date() : msg.message().forwardDate()).toLocaleString()).html().sync();
            msg.forwardTo(userId);

        }

        return true;
    }

}
