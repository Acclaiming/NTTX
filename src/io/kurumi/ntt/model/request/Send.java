package io.kurumi.ntt.model.request;

import cn.hutool.core.util.*;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.model.request.*;
import io.kurumi.ntt.utils.*;

public class Send extends AbstractSend<Send> {

    private SendMessage request;

    public Send(Fragment fragment,String chatId,String... msg) {

        this(null,fragment,chatId,msg);

    }

    public Send(Fragment fragment,long chatId,String... msg) {

        this(null,fragment,chatId,msg);

    }

    public Send(String chatId,String... msg) {

        this(null,Launcher.INSTANCE,chatId,msg);

    }

    public Send(long chatId,String... msg) {

        this(null,Launcher.INSTANCE,chatId,msg);

    }


    private Send(Void v,Fragment fragment,Object chatId,String... msg) {

        super(fragment);

        request = new SendMessage(chatId,ArrayUtil.join(msg,"\n"));

        this.fragment = fragment;

        request.disableWebPagePreview(true);

    }

    @Override
    public Send enableLinkPreview() {

        request.disableWebPagePreview(false);

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

    public Send hideKeyboard() {

        request.replyMarkup(new ReplyKeyboardHide());

        return this;

    }

    public Send removeKeyboard() {

        request.replyMarkup(new ReplyKeyboardHide());

        return this;

    }

    public Send keyboard(Keyboard keyboard) {

        request.replyMarkup(keyboard.markup());

        return this;

    }

    @Override
    public Send buttons(ButtonMarkup markup) {

        request.replyMarkup(markup.markup());

        return this;

    }

	
    public Msg send() {

        SendResponse resp = sync();

        if (!resp.isOk()) return null;

        return new Msg(fragment,resp.message());

    }

    @Override
    public BaseResponse sync(Exception track) {

        SendResponse resp = fragment.bot().execute(request);

        if (!resp.isOk()) {

			BotLog.info("消息发送失败 " + resp.errorCode() + " : " + resp.description(),track);

        }

        return resp;

    }




    @Override
    public SendResponse sync() {

        //     System.out.println(request.toWebhookResponse());

        SendResponse resp = fragment.bot().execute(request);

        if (!resp.isOk()) {

            BotLog.infoWithStack("消息发送失败 " + resp.errorCode() + " : " + resp.description());

        }

        return resp;

    }

}
