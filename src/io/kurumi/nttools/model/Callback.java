package io.kurumi.nttools.model;

import com.pengrad.telegrambot.model.CallbackQuery;
import io.kurumi.nttools.fragments.Fragment;
import io.kurumi.nttools.model.request.AnswerCallback;
import io.kurumi.nttools.utils.CData;
import cn.hutool.core.util.ArrayUtil;

public class Callback extends Msg {
    
    private CallbackQuery query;
    
    public CData data;
    
    public Callback(Fragment fragment, CallbackQuery query) {

        super(fragment,query.message());

        this.query = query;
        
        data = new CData(query.data());
        
    }
    
    public void confirm() {
        
        answer().exec();
        
    }
    
    public void text(String[] text) {

        answer().text(ArrayUtil.join(text,"\n")).exec();

    }
    
    public void text(String text) {

        answer().cacheTime(0).text(text).exec();

    }
    
    public void alert(String[] alert) {

        answer().alert(ArrayUtil.join(alert,"\n")).exec();

    }
    
    
    public void alert(String alert) {

        answer().alert(alert).exec();

    }
    
    public void url(String url) {

        answer().url(url).exec();

    }
    
    public AnswerCallback answer() {
        
        return new AnswerCallback(fragment,query.id());
        
    }
    
}
