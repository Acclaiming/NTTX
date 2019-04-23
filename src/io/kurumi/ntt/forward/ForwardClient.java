package io.kurumi.ntt.forward;

import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.model.request.*;
import java.util.*;
import cn.hutool.core.date.*;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.*;

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

    final String POINT_REPLY = "r";

    @Override
    public boolean onPrivate(UserData user,Msg msg) {

        if ("start".equals(msg.command())) {

            if (msg.params().length == 0) {

                msg.send("这里是 " + this.user.name() + " 的私聊BOT ✧٩(ˊωˋ*)و✧ 发送信息给咱就可以了 ~").html().exec();

            } else {

                UserData target = UserData.get(Long.parseLong(msg.params()[0]));

                if (target == null) {
                    
                    msg.send("找不到目标...").exec();

                    return true;

                }

                msg.send("回复 " + target.userName() + " 直接发送信息即可 (非文本，表情，图片，文件不会复制发送而是直接转发) : ").html().exec();

                setPoint(user,POINT_REPLY,target.id);

            }

        } else {

            new Send(this,userId,"来自 " + user.userName() + " : ",DateUtil.formatChineseDate(new Date(((long)(msg.message().forwardDate() == null ? msg.message().date() : msg.message().forwardDate())) * 1000),false)).html().sync();
            msg.forwardTo(userId);

        }

        return true;
    }

    @Override
    public boolean onPointedPrivate(UserData user,Msg msg) {
        
        PointStore.Point<Long> point = getPoint(user);
        
        Message message = msg.message();
        
        int sended = -1;
        
        if (message.document() != null) {
            
            SendDocument send = new SendDocument(msg.chatId(),message.document().fileId());
            
            send.fileName(message.document().fileName());

            send.caption(message.text());
            
            SendResponse resp = bot().execute(send);

            if (!resp.isOk()) {

                msg.send("发送失败 (˚☐˚! )/","-----------------------",resp.description()).exec();

            } else {
                
                sended = resp.message().messageId();
                
            }
            
        } else if (message.photo() != null) {

            for (PhotoSize size : message.photo()) {
                
                
                
            }

        }
        
        
        return true;

    }

}
