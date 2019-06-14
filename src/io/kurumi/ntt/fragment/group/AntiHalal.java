package io.kurumi.ntt.fragment.group;

import cn.hutool.json.JSONArray;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.User;
import io.kurumi.ntt.db.LocalData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.abs.Function;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.utils.NTT;

import java.security.acl.Group;
import java.util.LinkedList;

public class AntiHalal extends Function {

    public static AntiHalal INSTANCE = new AntiHalal();

    public static JSONArray enable = LocalData.getJSONArray("data", "anti_halal", true);

    public static void save() {

        LocalData.setJSONArray("data", "anti_halal", enable);

    }

    public boolean isHalal(String name) {

        boolean halel = false;
        boolean chinese = false;

        for (char chatAt : name.toCharArray()) {

            Character.UnicodeBlock current = Character.UnicodeBlock.of(chatAt);

            if (current == Character.UnicodeBlock.ARABIC) {

                halel = true;

            } else if (chatAt >= 0x4E00 && chatAt <= 0x9FA5) {

                chinese = true;

            }

        }

        return halel && !chinese;

    }

    @Override
    public void functions(LinkedList<String> names) {

        names.add("antihalal");

    }

    @Override
    public int target() {

        return Group;

    }


    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params) {

        if (NTT.checkGroupAdmin(msg)) return;

        if (params.length == 1 && "off".equals(params[0])) {

            if (!enable.contains(msg.chatId().longValue())) {

                msg.send("无需重复关闭 ~").exec();

            } else {

                enable.remove(msg.chatId().longValue());

                save();

                msg.send("关闭成功 ~").exec();

            }

        } else {

            if (enable.contains(msg.chatId().longValue())) {

                msg.send("没有关闭 ~").exec();

            } else {

                enable.add(msg.chatId().longValue());

                save();

                msg.send("已开启 ~").exec();

            }

        }

    }


    @Override
    public boolean onGroup(UserData user, Msg msg) {

        Message message = msg.message();

        if (message.newChatMembers() == null) return false;

        if (!enable.contains(msg.chatId())) return false;

        for (User newUser : message.newChatMembers()) {

            UserData newMember = UserData.get(newUser);

            if (isHalal(newMember.name())) {

                msg.delete();

                msg.kick(newMember.id);

                msg.send("已经吃掉一个清真 (" + newMember.userName() + ") Σ( ﾟωﾟ").html().exec();

                return true;

            }

        }


        return false;

    }

}
