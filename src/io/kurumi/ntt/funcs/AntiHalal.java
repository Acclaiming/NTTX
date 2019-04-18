package io.kurumi.ntt.funcs;

import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.*;
import com.pengrad.telegrambot.model.*;
import cn.hutool.json.*;
import io.kurumi.ntt.utils.*;
import java.lang.Character.*;

public class AntiHalal extends Fragment {

    public static AntiHalal INSTANCE = new AntiHalal();
    
    public static JSONArray enable = SData.getJSONArray("data","anti_halal",true);

    public static void save() {

        SData.setJSONArray("data","anti_halal",enable);

    }

    public boolean isHalal(String name) {

        boolean halel = false;
        boolean chinese = false;

        for (char chatAt : name.toCharArray()) {

            Character.UnicodeBlock current = Character.UnicodeBlock.of(chatAt);

            if (current == Character.UnicodeBlock.ARABIC) {

                halel = true;

            } else if (chatAt >= 0x4E00 &&  chatAt <= 0x9FA5) {

                chinese = true;

            }

        }

        return halel && !chinese;

    }

    @Override
    public boolean onGroupMsg(UserData user,Msg msg,boolean superGroup) {

        Message message = msg.message();

        if (message.newChatMembers() == null) return false;

        if (!enable.contains(msg.chatId())) return false;

        for (User newUser : message.newChatMembers()) {

            UserData newMember = BotDB.getUserData(newUser);

            if (isHalal(newMember.name())) {

                msg.delete();

                msg.kick(newMember.id);

                msg.send("已经吃掉一个清真 (" + newMember.userName() + ") Σ( ﾟωﾟ").html().exec();

                return true;

            }

        }

        

        return false;

    }
    

    @Override
    public boolean onMsg(UserData user,Msg msg) {

        if (!msg.isCommand()) return false;

        switch (msg.command()) {

            case "enableantihalal" : enable(user,msg);break;
            case "disableantiahalal" : disable(user,msg);break;

            default : return false;

        }

        return true;

    }

    void enable(UserData user,Msg msg) {

        if (T.checkGroup(msg)) return;
        if (T.checkGroupAdmin(msg)) return;

        if (enable.contains(msg.chatId())) {

            msg.send("无需重复开启 ( ˶‾᷄࿀‾᷅˵ )").publicFailed();

            return;

        }

        enable.add(msg.chatId());

        save();

        msg.send("已开启 ~ 请确认BOT有删除消息和禁止绒布球权限 ❀.(*´▽`*)❀.").exec();

    }

    void disable(UserData user,Msg msg) {

        if (T.checkGroup(msg)) return;
        if (T.checkGroupAdmin(msg)) return;

        if (!enable.contains(msg.chatId())) {

            msg.send("没有开启 ( ˶‾᷄࿀‾᷅˵ )").publicFailed();

            return;

        }

        enable.remove(msg.chatId());

        save();

        msg.send("已关闭 ~").exec();

    }



}
