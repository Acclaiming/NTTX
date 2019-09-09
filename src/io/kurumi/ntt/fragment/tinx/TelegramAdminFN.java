package io.kurumi.ntt.fragment.tinx;

import cn.hutool.core.util.NumberUtil;
import io.kurumi.ntt.db.GroupData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.Html;

public class TelegramAdminFN extends Fragment {

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerAdminFunction("tinx_bind", "tinx_unbind", "tinx_list");

    }

    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params) {

        if (function.endsWith("_bind")) {

            if (params.length < 2 || !NumberUtil.isNumber(params[0]) || !NumberUtil.isNumber(params[1])) {

                msg.invalidParams("chatId", "groupId").async();

                return;

            }

            TelegramBridge.GroupBind bind = new TelegramBridge.GroupBind();

            bind.id = NumberUtil.parseLong(params[0]);
            bind.groupId = NumberUtil.parseLong(params[1]);

            TelegramBridge.telegramIndex.put(bind.id, bind.groupId);
            TelegramBridge.qqIndex.put(bind.groupId, bind.id);

            TelegramBridge.data.setById(bind.id, bind);

            msg.send("完成 :)").async();

        } else if (function.endsWith("_unbind")) {

            if (params.length < 1 || !NumberUtil.isNumber(params[0])) {

                msg.invalidParams("chatId").async();

                return;

            }

            TelegramBridge.data.deleteById(NumberUtil.parseLong(params[0]));

            msg.send("完成 :)").async();

        } else if (function.endsWith("_list")) {

            String message = "所有群组 :\n";

            for (TelegramBridge.GroupBind bind : TelegramBridge.data.getAll()) {

                message += "\n" + Html.code(bind.id) + " ( " + GroupData.get(bind.id).title + " ) -> " + Html.code(bind.groupId);

            }

            msg.send(message).html().async();

        }

    }

}
