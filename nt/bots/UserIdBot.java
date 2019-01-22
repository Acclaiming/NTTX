package io.kurumi.nt.bots;

import twitter4j.*;
import io.kurumi.nt.*;
import cn.hutool.core.util.*;

public class UserIdBot extends StatusListenerBot {

    @Override
    public String getBotName() {
        return "UserIdBot";
    }

    @Override
    public String getConfigKey() {
        return "bot_userid_status";
    }

    @Override
    public void onStatus(Twitter api, Status status) throws TwitterException {
        
        String text = NTApi.getContext(status);
        
        if (!text.contains("@")) {

            try {

                long targetId = Long.parseLong(text);

                try {

                    User target =  api.showUser(targetId);

                    NTApi.reply(api, status, "目标用户 : \n" + NTApi.formatUsernName(target));

                } catch (TwitterException e) {

                    NTApi.reply(api, status, "目标UserId 「" + targetId + "」 无法取得\n请检查Id...");


                }

            } catch (Exception e) {}

            NTApi.reply(api, status, "您的UserId : " + status.getUser().getId());

        } else {

            String screenName = StrUtil.subAfter(text, "@", true);

            while (screenName.contains(" ")) {

                screenName = StrUtil.subBefore(screenName, " ", false);

            }

            try {

                User target =  api.showUser(screenName);

                NTApi.reply(api, status, "目标 " + NTApi.formatUsernName(target) + " :\n" + "UserId为 : " + target.getId());

            } catch (TwitterException e) {

                NTApi.reply(api, status, "目标 「" + screenName + "」 无法取得\n请检查用户名...");

            }

        }

    }

}
