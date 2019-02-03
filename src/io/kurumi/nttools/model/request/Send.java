package io.kurumi.nttools.model.request;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.log.StaticLog;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.model.request.ReplyKeyboardHide;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import io.kurumi.nttools.fragments.Fragment;
import io.kurumi.nttools.model.Msg;

public class Send extends AbstractSend<Send> {

    private Fragment fragment;
    private SendMessage request;

    public Send(Fragment fragment, Object chatId, String... msg) {

        request = new SendMessage(chatId, ArrayUtil.join(msg, "\n"));

        this.fragment = fragment;


    }

    @Override
    public Send disableLinkPreview() {

        request.disableWebPagePreview(true);

        return this;

    }


    public Send disableNotification() {

        request.disableNotification(true);

        return this;

    }

    public Send replyToMessageId(int replyToMessageId) {

        request.replyToMessageId(replyToMessageId);

        return this;

    }


    public Send replyTo(Msg msg) {

        replyToMessageId(msg.messageId());

        return this;

    }


    @Override
    public Send markdown() {

        request.parseMode(ParseMode.Markdown);

        return this;

    }

    @Override
    public Send html() {

        request.parseMode(ParseMode.HTML);

        return this;

    }

    public Send hideKeyBoard() {

        request.replyMarkup(new ReplyKeyboardHide());

        return this;

    }

    public Send removeKeyBoard() {

        request.replyMarkup(new ReplyKeyboardHide());

        return this;

    }

    @Override
    public Send buttons(ButtonMarkup markup) {

        request.replyMarkup(markup.markup());

        return this;

    }

    public Msg send() {

        return new Msg(fragment, exec().message());

    }

    @Override
    public SendResponse exec() {
        SendResponse resp = fragment.bot.execute(request);

        if (!resp.isOk()) {

            StaticLog.error(new RuntimeException(),"SendMseeage Error " + resp.errorCode() + " : " + resp.description());

        }

        return resp;

    }

}
