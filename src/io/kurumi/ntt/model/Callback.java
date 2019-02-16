package io.kurumi.ntt.model;

import cn.hutool.core.util.ArrayUtil;
import com.pengrad.telegrambot.model.CallbackQuery;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.request.AnswerCallback;
import io.kurumi.ntt.utils.CData;

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
