package io.kurumi.ntt.fragment.extra;

import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.response.SendResponse;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.Html;

public class ShowFile extends Fragment {

    public static String createHtmlPayload(Fragment f, String text, String fileId) {

        return Html.a(text, createPayload(f, fileId));

    }

    public static String createPayload(Fragment f, String fileId) {

        return "https://t.me/" + f.origin.me.username() + "?start=file" + PAYLOAD_SPLIT + fileId;

    }

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerFunction("file");

        registerPayload("file");

    }

    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params) {

        if (params.length == 0) {

            msg.invalidParams("fileId").async();

            return;

        }

        SendResponse resp = execute(new SendDocument(msg.chatId(), params[0]).caption("文件分享链接 : " + createPayload(this, params[0])).parseMode(ParseMode.HTML));

        if (!resp.isOk()) {

            msg.send("读取文件失败 : " + resp.description()).async();

        }

    }

    @Override
    public void onPayload(UserData user, Msg msg, String payload, String[] params) {

        if (params.length == 0) {

            msg.invalidQuery();

            return;

        }

        SendResponse resp = execute(new SendDocument(msg.chatId(), params[0]).caption("文件分享链接 : " + createPayload(this, params[0])).parseMode(ParseMode.HTML));

        if (!resp.isOk()) {

            msg.send("读取文件失败 : " + resp.description()).async();

        }

    }

}
