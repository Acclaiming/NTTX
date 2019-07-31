package io.kurumi.ntt.fragment.group;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import io.kurumi.ntt.db.GroupData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.NTT;

import java.util.ArrayList;

public class BanSetickerSet extends Fragment {

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerFunction("banss", "unbanss");

    }

    @Override
    public int checkFunctionContext(UserData user, Msg msg, String function, String[] params) {

        return FUNCTION_GROUP;

    }

    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params) {

        if (user.blocked()) {

            msg.send("你不能这么做 (为什么？)").async();

            return;

        }

        if ("banss".equals(function)) {

            if (NTT.checkGroupAdmin(msg)) return;

            String setName = null;

            if (msg.replyTo() == null || msg.replyTo().message().sticker() == null) {

                if (msg.params().length == 0) {

                    msg.send("/banss <贴纸集名称> 或者对sticker使用啦...").publicFailed();

                    return;

                }

                setName = msg.params()[0];

            } else {

                setName = msg.replyTo().message().sticker().setName();

            }

            GroupData data = GroupData.get(msg.chat());

            if (data.ban_sticker_set != null && data.ban_sticker_set.contains(setName)) {

                msg.send("这个贴纸包已经被屏蔽了 :)").publicFailed();

                return;

            } else if (data.ban_sticker_set == null) {

                data.ban_sticker_set = new ArrayList<>();

            }

            data.ban_sticker_set.add(setName);

            msg.send("屏蔽成功 ~").exec();

        } else if ("unbanss".equals(function)) {

            if (NTT.checkGroupAdmin(msg)) return;

            String setName = null;

            if (msg.replyTo() == null || msg.replyTo().message().sticker() == null) {

                if (msg.params().length == 0) {

                    msg.send("/unbanss <贴纸集名称> 或者对sticker使用啦...").publicFailed();

                    return;

                }

                setName = msg.params()[0];

            } else {

                setName = msg.replyTo().message().sticker().setName();

            }

            GroupData data = GroupData.get(msg.chat());

            if (data.ban_sticker_set == null || !data.ban_sticker_set.contains(setName)) {

                msg.send("这个贴纸包没有被屏蔽 :)").publicFailed();

                return;

            }

            data.ban_sticker_set.remove(setName);

            if (data.ban_sticker_set.isEmpty()) data.ban_sticker_set = null;

            msg.send("屏蔽成功 ~").exec();
        }

    }

    @Override
    public boolean msg() {

        return true;

    }


    @Override
    public void onGroup(UserData user, Msg msg) {

        if (msg.isGroup() && msg.sticker() != null) {

            GroupData data = GroupData.get(msg.chat());

            if (data.ban_sticker_set != null && data.ban_sticker_set.contains(msg.sticker().setName())) {

                msg.delete();

            }

        }

    }

}


