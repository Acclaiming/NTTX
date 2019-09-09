package io.kurumi.ntt.fragment.bots;

import cn.hutool.log.StaticLog;
import io.kurumi.ntt.db.Data;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.BotServer;

import java.util.Map;

public class UserBot {

    public static Data<UserBot> data = new Data<UserBot>("UserCustomBot", UserBot.class);

    public Long id;
    public String userName;
    public Long user;
    public String token;
    public int type;
    public Map<String, Object> params;

    public static void startAll() {

        for (final UserBot bot : data.collection.find()) {

			/*
			
			if (Firewall.block.containsId(bot.user)) {

				data.deleteById(bot.id);

				// new Send(bot.user,"你的机器人 @" + bot.userName + " 的已从NTT取消接管。(为什么？)").exec();

				continue;

			}
			
			*/

            BotFragment.waitPool.execute(new Runnable() {

                @Override
                public void run() {

                    bot.startBot();

                }

            });

        }

    }

    public boolean startBot() {

        if (!BotServer.fragments.containsKey(token)) {

            try {

                UserBotFragment bot;

                if (type == 0) {

                    ForwardBot client = new ForwardBot();

                    client.botId = id;

                    client.silentStart();

                    bot = client;

                } else if (type == 1) {

                    GroupBot client = new GroupBot();

                    client.botId = id;
                    client.silentStart();

                    bot = client;

                } else if (type == 2) {

                    RssBot client = new RssBot();

                    client.botId = id;
                    client.silentStart();

                    bot = client;

                } else return false;

            } catch (Exception e) {

                StaticLog.info("机器人令牌已失效 : {}" + userName);

                // data.deleteById(id);

                //  new Send(user,"对不起，但是你的机器人 : @" + userName + " 的令牌已失效，已自动移除。").exec();

                return false;

            }

        }

        return true;

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

        }

        return information.toString();

    }

    public String typeName() {

        switch (type) {

            case 0:
                return "私聊";

            case 1:
                return "群组管理";

            case 2:
                return "RSS";

            default:
                return null;

        }

    }

}
