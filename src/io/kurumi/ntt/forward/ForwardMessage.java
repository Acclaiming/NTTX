package io.kurumi.ntt.forward;

import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.*;
import cn.hutool.json.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.model.request.*;
import java.util.*;

public class ForwardMessage extends Fragment {

    public static ForwardMessage INSTANCE = new ForwardMessage();
    
    public static JSONObject bots = SData.getJSON("data","chat_bot",true);

    public static void start() {
        
        for (Map.Entry<String,Object> bot : bots.entrySet()) {
            
            String token = (String)bot.getValue();
            
            BotServer.fragments.put(token,new ForwardClient(Long.parseLong(bot.getKey()),token));
            
        }
        
    }

    public static void save() {

        SData.setJSON("data","chat_bot",bots);

    }  

    @Override
    public boolean onMsg(UserData user,Msg msg) {

        if (!msg.isCommand()) return false;

        switch (msg.command()) {

                case "chatbot" : setChatBot(user,msg);break;
                case "rmchatbot" : removeChatBot(user,msg);break;

                default : return false;

        }

        return true;

    }

    final String POINT_INPUT_TOKEN = "m|i";

    void setChatBot(UserData user,Msg msg) {

        if (bots.containsKey(user.id.toString())) {

            msg.send("你已经设置了一个Bot ~ 使用 /rmchatbot 移除它 < (ˉ^ˉ)> ").exec();
            return;

        }

        msg.send("这个功能可以创建一个转发所有私聊到乃的BOT ~o(〃'▽'〃)o").exec();
        msg.send("现在输入 BotToken 这需要在 @BotFather 申请 ~ 或者使用 /cancel 取消设置。").exec();

        user.point = cdata(POINT_INPUT_TOKEN);

    }

    @Override
    public boolean onPPM(UserData user,Msg msg) {

        switch (user.point.getPoint()) {

                case POINT_INPUT_TOKEN : onInputToken(user,msg);break;
                default : return false;

        }

        return true;

    }

    void onInputToken(UserData user,Msg msg) {

        if (!msg.hasText()) {

            msg.send("请输入用于转发私聊信息的BotToken (๑• . •๑) 取消输入使用 /cancel ~").exec();

            return;

        }

        String token = msg.text();

        if (!Env.verifyToken(token)) {

            msg.send("无效的BotToken... 取消使用 /cancel ~").exec();

            return;

        }

        bots.put(user.id.toString(),token);

        save();

        ForwardClient client = new ForwardClient(user.id,token);

        BotServer.fragments.put(token,client);

        client.silentStart();

        msg.send("乃的Bot : " + BotDB.getUserData(client.me) + " 已经启动 ~").html().exec();
        msg.send("别忘记发送一条信息给Bot哦 ~ Bot不能主动给乃发送信息 ~o(〃'▽'〃)o").exec();


    }

    void removeChatBot(UserData user,Msg msg) {

        if (bots.containsKey(user.id.toString())) {

            bots.remove(user.id.toString());

            save();

            msg.send("移除成功 ~").exec();

        } else {

            msg.send("乃没有设置私聊转发Bot (๑• . •๑) ").exec();

        }

    }

}
