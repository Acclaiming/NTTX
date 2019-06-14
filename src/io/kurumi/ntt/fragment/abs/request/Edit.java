package io.kurumi.ntt.fragment.abs.request;

import cn.hutool.core.util.ArrayUtil;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.response.BaseResponse;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.utils.BotLog;

public class Edit extends AbstractSend<Edit> {

    private EditMessageText request;

    public Edit(Fragment fragment, Object chatId, int messageId, String... msg) {

        super(fragment);

        request = new EditMessageText(chatId, messageId, ArrayUtil.join(msg, "\n"));

        this.fragment = fragment;

        request.disableWebPagePreview(true);


    }

    @Override
    public Edit enableLinkPreview() {

        request.disableWebPagePreview(false);

        return this;

    }

    @Override
    public Edit markdown() {

        request.parseMode(ParseMode.Markdown);

        return this;

    }

    @Override
    public Edit html() {

        request.parseMode(ParseMode.HTML);

        return this;

    }

    @Override
    public Edit buttons(ButtonMarkup markup) {

        request.replyMarkup(markup.markup());

        return this;

    }

    public void publicFailedWith(final Msg message) {

        if (message.isPrivate()) {

            exec();

        } else {

            failedWith(5000, message);

        }

    }

    public void failedWith(final long delay, final Msg message) {

        if (origin == null) return;

        BaseResponse resp = exec();

        if (resp != null && resp.isOk()) {

            io.kurumi.ntt.utils.NTT.tryDelete(delay, message, origin);

        }

    }

    public Edit withCancel() {

        request.setText(request.getText() + "\n\n" + "使用 /cancel 取消 ~");

        return this;

    }

    @Override
    public BaseResponse exec() {

        //  System.out.println(request.toWebhookResponse());   

        try {

            BaseResponse resp = fragment.bot().execute(request);

            //    if (resp.errorCode() ==

            if (resp != null && !resp.isOk()) {

                BotLog.infoWithStack("消息发送失败 " + resp.errorCode() + " : " + resp.description());

            }

            return resp;

        } catch (Exception ex) {

            return null;

        }


    }

}
