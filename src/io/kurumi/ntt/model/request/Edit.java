package io.kurumi.ntt.model.request;

import cn.hutool.core.util.ArrayUtil;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.response.BaseResponse;
import io.kurumi.ntt.fragment.Fragment;
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

    @Override
    public BaseResponse sync() {

        //  System.out.println(request.toWebhookResponse());

        BaseResponse resp = fragment.bot().execute(request);

        //    if (resp.errorCode() ==

        BotLog.debug("信息发送失败 : " + resp.description());
        

        return resp;

    }

}
