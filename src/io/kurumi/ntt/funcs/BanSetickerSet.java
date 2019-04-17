package io.kurumi.ntt.funcs;

import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.*;
import cn.hutool.json.*;
import io.kurumi.ntt.utils.*;

public class BanSetickerSet extends Fragment {

    public static BanSetickerSet INSTANCE = new BanSetickerSet();
    public static JSONObject bans = SData.getJSON("data","ban_sticker_set",true);

    public static void save() {

        SData.setJSON("data","ban_sticker_set",bans);

    }

    @Override
    public boolean onGroupMsg(UserData user,Msg msg,boolean superGroup) {

        if (msg.isCommand()) {

            switch (msg.command()) {

                case "banstickerset" : banStickerSet(user,msg);break;
                case "unbanstickerset" : unBanStickerSet(user,msg);break;
                    
                default : return false;

            }

            return true;

        } else if (msg.message().sticker() != null && bans.containsKey(msg.chatId().toString())) {

            if (bans.getJSONArray(msg.chatId().toString()).contains(msg.message().sticker().setName())) {
                
                msg.delete();
                
                return true;
                
            }

        }
        
        return false;

    }

    void banStickerSet(UserData user,Msg msg) {

        if (T.checkGroupAdmin(msg)) return;

        if (msg.replyTo() == null || msg.replyTo().message().sticker() == null) {

            msg.send("请对sticker使用啦...").publicFailed();

            return;

        }

        String setName = msg.replyTo().message().sticker().setName();

        JSONArray rules = bans.getJSONArray(msg.chatId().toString());

        if (rules == null) rules = new JSONArray();

        if (rules.contains(setName)) {

            msg.send("这个贴纸包已经被屏蔽了 :)").publicFailed();

            return;

        }

        rules.add(setName);
        
        bans.put(msg.chatId().toString(),rules);
        
        save();

        msg.send("屏蔽成功 ~").exec();

    }

    void unBanStickerSet(UserData user,Msg msg) {

        if (T.checkGroupAdmin(msg)) return;

        if (msg.replyTo() == null || msg.replyTo().message().sticker() == null) {

            msg.send("请对sticker使用啦...").publicFailed();

            return;

        }

        String setName = msg.replyTo().message().sticker().setName();

        JSONArray rules = bans.getJSONArray(msg.chatId().toString());

        if (rules == null) rules = new JSONArray();

        if (!rules.contains(setName)) {

            msg.send("这个贴纸包没有被屏蔽了 :)").publicFailed();

            return;

        }

        rules.remove(setName);
        
        bans.put(msg.chatId().toString(),rules);

        save();

        msg.send("取消屏蔽成功 ~").exec();

    }
}
