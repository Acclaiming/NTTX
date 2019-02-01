package io.kurumi.nttools.model.request;

import com.pengrad.telegrambot.response.BaseResponse;

public abstract class AbstractSend<T extends AbstractSend> {

    public abstract T buttons(ButtonMarkup markup);
    
    public abstract T disableLinkPreview();
    
    public abstract T markdown();
    
    public abstract T html();
    
    public abstract BaseResponse exec();

}
