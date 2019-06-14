package io.kurumi.ntt.fragment.abs.request;

import com.pengrad.telegrambot.response.BaseResponse;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.abs.Msg;

public abstract class AbstractSend<T extends AbstractSend> {

    public Msg origin;

    public Fragment fragment;

    public AbstractSend(Fragment fragment) {
        this.fragment = fragment;
    }

    public abstract T buttons(ButtonMarkup markup);

    public abstract T enableLinkPreview();

    public abstract T markdown();

    public abstract T html();

    public abstract BaseResponse exec();

}
