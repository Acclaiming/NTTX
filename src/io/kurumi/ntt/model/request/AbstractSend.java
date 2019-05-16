package io.kurumi.ntt.model.request;

import com.pengrad.telegrambot.response.*;
import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.utils.*;

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
