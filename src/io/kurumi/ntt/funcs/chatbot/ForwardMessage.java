package io.kurumi.ntt.funcs.chatbot;

import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.*;
import cn.hutool.json.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.model.request.*;
import java.util.*;
import io.kurumi.ntt.funcs.abs.Function;

public class ForwardMessage extends Function {

    public static ForwardMessage INSTANCE = new ForwardMessage();

    public static JSONObject bots = LocalData.getJSON("data","chat_bot",true);

    public static void start() {

        for (Map.Entry<String,Object> bot : bots.entrySet()) {

            String token = (String)bot.getValue();

            UserData user = UserData.get(Long.parseLong(bot.getKey()));

            if (user != null) {

                ForwardClient client = new ForwardClient(user,token);

                client.silentStart();

            }

        }

    }

    public static void save() {

        LocalData.setJSON("data","chat_bot",bots);

    }

    @Override
    public void functions(LinkedList<String> names) {

        names.add("chatbot");

    }

    @Override
    public int target() {
        
        return Private;

    }

    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params) {

        if (params.length == 0) {
            
            msg.send("/chatbot <set|del> 设置或删除转发BOT").exec();
            
        } else if ("set".equals(params[0])) {
            
            if (bots.containsKey(user.id.toString())) {

                msg.send("你已经设置了一个Bot ~ 使用 /chatbot del 移除它 < (ˉ^ˉ)> ").exec();
                return;

            }

                msg.send("这个功能可以创建一个转发所有私聊到乃的BOT ~o(〃'▽'〃)o").exec();
                msg.send("现在输入 BotToken 这需要在 @BotFather 申请 ~ 或者使用 /cancel 取消设置。").exec();

                setPoint(user,POINT_INPUT_TOKEN);
            
        } else if ("del".equals(params[0])) {
            
            if (bots.containsKey(user.id.toString())) {

                bots.remove(user.id.toString());

                save();

                msg.send("移除成功 ~").exec();

            } else {

                msg.send("乃没有设置私聊转发Bot (๑• . •๑) ").exec();

            }
            
        } else {
            
            msg.send("/chatbot <set|del> 设置或删除转发BOT").exec();
            
            
        }

    }
    
    final String POINT_INPUT_TOKEN = "m|i";

    @Override
    public boolean onPointedPrivate(UserData user,Msg msg) {

        switch (getPoint(user).point) {

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

        ForwardClient client = new ForwardClient(user,token);

        client.silentStart();

        msg.send("乃的Bot : @" + client.me.username() + " 已经启动 ~").html().exec();
        msg.send("别忘记发送一条信息给Bot哦 ~ Bot不能主动给乃发送信息 ~o(〃'▽'〃)o").exec();

        clearPoint(user);

    }
    
}
