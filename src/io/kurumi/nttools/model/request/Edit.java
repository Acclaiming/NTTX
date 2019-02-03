package io.kurumi.nttools.model.request;

import cn.hutool.core.util.ArrayUtil;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.model.request.ReplyKeyboardHide;
import com.pengrad.telegrambot.request.EditMessageText;
import io.kurumi.nttools.fragments.Fragment;
import io.kurumi.nttools.model.Msg;
import com.pengrad.telegrambot.response.BaseResponse;
import cn.hutool.log.StaticLog;

public class Edit extends AbstractSend<Edit> {
    
    private Fragment fragment;
    private EditMessageText request;

    public Edit(Fragment fragment, Object chatId,int messageId, String... msg) {

        request = new EditMessageText(chatId,messageId,ArrayUtil.join(msg, "\n"));

        this.fragment = fragment;


    }

    @Override
    public Edit disableLinkPreview() {
        
        request.disableWebPagePreview(true);
        
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
    public BaseResponse exec() {

        BaseResponse resp = fragment.bot.execute(request);
        
        if (!resp.isOk()) {
            
            StaticLog.error(new RuntimeException(),"EditMseeage Error " + resp.errorCode() + " : " + resp.description());
            
        }
        
        return resp;

    }
}
