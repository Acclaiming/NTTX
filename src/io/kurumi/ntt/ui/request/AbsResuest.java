package io.kurumi.ntt.ui.request;
import com.pengrad.telegrambot.response.*;

public interface AbsResuest {
    
    public BaseResponse exec();
    public String toWebHookResp();
    
}
