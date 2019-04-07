package io.kurumi.ntt.model.request;

import cn.hutool.core.util.*;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.*;
import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.utils.*;

public class Edit extends AbstractSend<Edit> {

    private EditMessageText request;

    public Edit(Fragment fragment,Object chatId,int messageId,String... msg) {

        super(fragment);

        request = new EditMessageText(chatId,messageId,ArrayUtil.join(msg,"\n"));

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

            failedWith(5000,message);

        }

    }

    public void failedWith(final long delay,final Msg message) {

        if (origin == null) return;

        final Exception track = new Exception();

        ThreadPool.exec(new Runnable() {

                @Override
                public void run() {

                    BaseResponse resp = sync(track);

                    if (resp != null && resp.isOk()) {

                        io.kurumi.ntt.utils.T.tryDelete(delay,message,origin);

                    }


                }

            });

    }


    @Override
    public BaseResponse sync() {

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

    @Override
    public BaseResponse sync(Exception track) {

        try {

            BaseResponse resp = fragment.bot().execute(request);

            //    if (resp.errorCode() ==

            if (resp != null && !resp.isOk()) {

                BotLog.info("消息发送失败 " + resp.errorCode() + " : " + resp.description(),track);

            }

            return resp;


        } catch (Exception ex) {

            return null;

        }
        
    }








}
