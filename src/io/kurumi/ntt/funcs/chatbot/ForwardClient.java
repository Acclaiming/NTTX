package io.kurumi.ntt.funcs.chatbot;

import cn.hutool.core.date.DateUtil;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.response.SendResponse;
import io.kurumi.ntt.db.PointStore;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.Send;
import java.util.Date;
import io.kurumi.ntt.utils.Html;
import com.pengrad.telegrambot.request.SendSticker;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.ForwardMessage;
import cn.hutool.core.util.StrUtil;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.response.BaseResponse;

public class ForwardClient extends BotFragment {

    public Long userId;
    public String botToken;

    public Long lastReceivedFrom;

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

                msg.send("这里是 " + this.user.name() + " 的BOT ✧٩(ˊωˋ*)و✧ 发送信息给咱就可以了 ~").html().exec();
                
            } else {
                
                msg.delete();

                if (msg.params()[0].startsWith("r")) {

                    UserData target = UserData.get(Long.parseLong(msg.params()[0].substring(1)));

                    if (target == null) {

                        msg.send("找不到目标...").exec();

                        return true;

                    }

                    msg.send("回复 " + target.userName() + " : " ,"直接发送信息即可 (非文本，表情，文件 会直接转发) : ","使用 /cancel 退出").html().exec();

                    setPoint(user,POINT_REPLY,target.id);
                    
                } else if (msg.params()[0].startsWith("d")) {
                    
                    try {
                    
                    long target = Long.parseLong(StrUtil.subBetween(msg.params()[0],"d","-"));
                    int messageId = Integer.parseInt(StrUtil.subAfter(msg.params()[0],"-",false));
                    
                    BaseResponse resp = bot().execute(new DeleteMessage(target,messageId));

                    if (resp.isOk()) {
                        
                        msg.send("已删除").exec();
                        
                    } else {
                        
                        msg.send("删除失败 这条发送的信息还在吗 ？").exec();
                        
                    }
                    
                    } catch (NumberFormatException e) {
                        
                        msg.send("这个删除已经点过了 :) (" + msg.params()[0]).exec();
                        
                    }
                    
                } else if (msg.params()[0].startsWith("b")) {
                }
                
            }
            
            return true;

        } else if (getPoint(user) == null) {

            if (lastReceivedFrom  == null || !lastReceivedFrom.equals(user.id))  {

                new Send(this,userId,"来自 " + user.userName() + " : [ " + Html.a("回复","https://t.me/" + me.username() +"?start=r" + user.id) + " ]").html().exec();

                lastReceivedFrom = user.id;

            }

            msg.forwardTo(userId);
            
            return true;

        }

        return false;
    }

    @Override
    public boolean onPointedPrivate(UserData user,Msg msg) {

        if (onPrivate(user,msg)) return true;
        
        PointStore.Point<Long> point = getPoint(user);

        long target = point.data;
        
        if (POINT_REPLY.equals(point.point)) {

            Message message = msg.message();

            int sended = -1;

            if (message.document() != null) {

                SendDocument send = new SendDocument(target,message.document().fileId());

                send.fileName(message.document().fileName());

                send.caption(message.text());

                SendResponse resp = bot().execute(send);

                if (!resp.isOk()) {

                    msg.send("发送失败 (˚☐˚! )/","-----------------------",resp.description()).exec();

                } else {
                    
                    sended = resp.message().messageId();

                }

            } else if (message.sticker() != null) {
                
                SendSticker send = new SendSticker(target,message.sticker().fileId());

                SendResponse resp = bot().execute(send);

                if (!resp.isOk()) {

                    msg.send("发送失败 (˚☐˚! )/","-----------------------",resp.description()).exec();

                } else {

                    sended = resp.message().messageId();

               }
                
            } else if (msg.hasText()) {
                
                SendMessage send = new SendMessage(target,msg.text());

                SendResponse resp = bot().execute(send);

                if (!resp.isOk()) {

                    msg.send("发送失败 (˚☐˚! )/","-----------------------",resp.description()).exec();

                } else {

                    sended = resp.message().messageId();

                }
                
            } else {
                
                ForwardMessage forward = new ForwardMessage(target,msg.chatId(),msg.messageId());

                SendResponse resp = bot().execute(forward);

                if (!resp.isOk()) {

                    msg.send("发送失败 (˚☐˚! )/","-----------------------",resp.description()).exec();

                } else {

                    sended = resp.message().messageId();

                }
                
            }
            
            if (sended != -1) {
                
                msg.reply("发送成功 [ " + Html.a("删除","https://t.me/" + me.username() + "?start=d" + target + "-" + sended) + " ]").html().exec();
                
            }
            

        }

        return true;


    }

}
