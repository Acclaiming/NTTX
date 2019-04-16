package io.kurumi.ntt.forward;

import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.*;

public class ForwardClient extends BotFragment {
    
    public Long userId;
    public String botToken;
    
    public ForwardClient(Long userId,String botToken)  {
        this.userId = userId;
        this.botToken = botToken;
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
    public boolean onNPM(UserData user,Msg msg) {
        msg.forwardTo(userId);
        return true;
    }

}
