package io.kurumi.ntt.fragment.bots;

import io.kurumi.ntt.db.Data;
import io.kurumi.ntt.fragment.bots.UserBot;

import java.util.Map;
import java.util.HashMap;

import io.kurumi.ntt.fragment.BotServer;

public class UserBot {

    public static Data<UserBot> data = new Data<UserBot>("UserCustomBot", UserBot.class);
    public Long id;
    public String userName;
    public Long user;
    public String token;
    public int type;
    public Map<String, Object> params;

    public static void startAll() {

        for (UserBot bot : data.collection.find()) {

            bot.startBot();

        }

    }

    public void startBot() {

        if (!BotServer.fragments.containsKey(token)) {

            if (type == 0) {

                ForwardBot client = new ForwardBot();

                client.botId = id;
                client.silentStart();

            } else if (type == 1) {

                JoinCaptchaBot client = new JoinCaptchaBot();

                client.botId = id;
                client.silentStart();

            }

        }

    }

    public void stopBot() {

        if (BotServer.fragments.containsKey(token)) {

            BotServer.fragments.remove(token).stop();

        }

    }

    public void reloadBot() {

        if (BotServer.fragments.containsKey(token)) {

            BotServer.fragments.get(token).reload();

        }

    }

    public String information() {

        StringBuilder information = new StringBuilder();

        if (type == 0) {

            String welcomeMsg = (String) params.get("msg");

            information.append("欢迎语 : > ").append(welcomeMsg).append(" <");

        } else if (type == 1) {

            Boolean delJoin = (Boolean) params.get("delJoin");

            if (delJoin == null) delJoin = false;

            information.append("删除加群退群消息 : ").append(delJoin ? "开启" : "关闭");

            Long logChannel = (Long) params.get("logChannel");

            information.append("\n日志频道 : " + (logChannel == null ? "未设置" : logChannel));
            
            String welcomeMsg = (String) params.get("welcome");

            if (welcomeMsg == null) welcomeMsg = "未设置";
            
            information.append("\n欢迎语 : > ").append(welcomeMsg).append(" <");
            
            Boolean delLast = (Boolean) params.get("delLast");

            if (delLast == null) delJoin = false;

            information.append("\n仅保留最新欢迎信息 : ").append(delJoin ? "开启" : "关闭");
            
            
        }

        return information.toString();

    }

    public String typeName() {

        switch (type) {

            case 0:
                return "私聊";

            case 1:
                return "加群验证";

            default:
                return null;

        }

    }

}
