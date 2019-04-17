package io.kurumi.ntt.forward;

import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.model.request.*;

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

        if (user.id == userId && msg.replyTo() != null && msg.replyTo().from().id.equals(me.id())) {
            
            Msg replyTo = msg.replyTo();
            
            if (replyTo.message().forwardFromChat() != null) {
                
                new Send(this,replyTo.from().id,msg.text()).replyToMessageId(replyTo.message().forwardFromMessageId()).exec();
                
                msg.reply("回复成功 ~").exec();
                
            }

        }
        
        if (!msg.isCommand()) {

            msg.forwardTo(userId);

        } else {

            if ("start".equals(msg.command())) {

                msg.send("这里是 " + user.userName() + " 的私聊BOT ✧٩(ˊωˋ*)و✧ 发送信息给咱就可以了 ~").html().exec();

            }

        }

        return true;
    }

}
