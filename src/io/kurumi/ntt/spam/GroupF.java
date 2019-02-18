package io.kurumi.ntt.spam;

import com.pengrad.telegrambot.model.User;
import io.kurumi.ntt.db.BotDB;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.ButtonMarkup;
import io.kurumi.ntt.stickers.DVANG;
import io.kurumi.ntt.twitter.TwitterUI;

import java.util.HashMap;
import java.util.Map;

public class GroupF extends Fragment {

    public static final String KEY = "NTT_GR";
    private static final String POINT_PASS = "g|p";
    private static final String POINT_REJ = "g|r";
    public static HashMap<Long, Boolean> cache = new HashMap<>();

    static {

        Map<String, String> all = BotDB.jedis.hgetAll(KEY);

        for (Map.Entry<String, String> tag : all.entrySet()) {

            cache.put(Long.parseLong(tag.getKey()), Boolean.parseBoolean(tag.getValue()));

        }

    }

    public static boolean isGroupEnable(long chatId) {

        return cache.containsKey(chatId) ? cache.get(chatId) : false;

    }

    public static void setGroupEnable(Long chatId, Boolean enable) {

        cache.put(chatId, enable);

        BotDB.jedis.hset(KEY, chatId.toString(), enable.toString());

    }

    @Override
    public boolean onGroupMsg(UserData user, Msg msg, boolean superGroup) {

        if (msg.message().newChatMembers() != null && isGroupEnable(msg.chatId())) {

            for (User n : msg.message().newChatMembers()) {

                UserData newMember = UserData.get(n);

                if (newMember.isBot) continue;

                onNewNember(newMember, msg);

            }

            return true;


        }

        return false;

    }

    private void onNewNember(final UserData user, final Msg msg) {

        msg.sendSticker(DVANG.发情);

        if (user.cTU() != null) {

            msg.send("你好 " + user.cTU().formatedNameMarkdown() + " 欢迎加群 ( ￣▽￣)σ").markdown().exec();

        }

        StringBuilder notice = new StringBuilder();

        notice.append("你好 ").append(user.userName()).append("，欢迎加群 ( ￣▽￣)σ\n\n");

        notice.append("为了确认你不是视奸号 请点下方按钮认证Twitter账号 、 或者私聊管理员要求通过");

        msg.send(notice.toString()).buttons(new ButtonMarkup() {{

            newUrlButtonLine("认证账号", TwitterUI.INSTANCE.pre(user, msg));

            newButtonLine()
                    .newButton("放行", POINT_PASS, user.id.toString())
                    .newButton("滥权", POINT_REJ, user.id.toString());

        }}).exec();

    }

    @Override
    public boolean onCallback(UserData user, Callback callback) {

        switch (callback.data.getPoint()) {

            //     case POINT_PASS : passUser();

        }

        return true;

    }

}
