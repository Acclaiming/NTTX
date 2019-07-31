package io.kurumi.ntt.fragment.debug;

import cn.hutool.json.JSONObject;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.Html;

public class DebugMsg extends Fragment {

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerAdminFunction("get_msg");

    }

    @Override
    public int checkFunctionContext(UserData user, Msg msg, String function, String[] params) {

        return FUNCTION_PUBLIC;

    }

    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params) {

        if (msg.replyTo() == null) {

            msg.send("没有对消息回复").exec();

            return;

        }

        if (msg.update != null) {

            msg.send(Html.code(new JSONObject(msg.update.json).getByPath("message.reply_to_message"))).html().exec();

        } else {

            msg.send(msg.replyTo().message().toString()).exec();

        }

    }

}
