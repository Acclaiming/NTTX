package io.kurumi.ntt.fragment.debug;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.pengrad.telegrambot.request.GetStickerSet;
import com.pengrad.telegrambot.response.GetStickerSetResponse;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.Html;

public class DebugStickerSet extends Fragment {

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerAdminFunction("get_sticker_set");

    }

    @Override
    public int checkFunctionContext(UserData user, Msg msg, String function, String[] params) {

        return FUNCTION_PUBLIC;

    }

    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params) {

        String target;

        if (params.length > 0) {

            target = params[0];

            if (target.contains("/")) target = StrUtil.subAfter(target, "/", true);

        } else if (msg.replyTo().sticker() != null) {

            target = msg.replyTo().sticker().setName();

            if (target == null) {

                msg.send("这个贴纸没有贴纸包").publicFailed();

                return;

            }

        } else {

            return;

        }

        final GetStickerSetResponse set = bot().execute(new GetStickerSet(target));

        if (!set.isOk()) {

            msg.send("无法读取贴纸包 " + target + " : " + set.description()).exec();

            return;

        }

        msg.send(new JSONObject(set.json).toStringPretty()).exec();

    }

}
