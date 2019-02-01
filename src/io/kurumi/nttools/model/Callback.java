package io.kurumi.nttools.model;

import com.pengrad.telegrambot.model.CallbackQuery;
import io.kurumi.nttools.fragments.Fragment;
import io.kurumi.nttools.model.request.AnswerCallback;
import io.kurumi.nttools.utils.CData;

public class Callback extends Msg {
    
    private CallbackQuery query;
    
    public Callback(Fragment fragment, CallbackQuery query) {

        super(fragment,query.message());

        this.query = query;
        
    }
    
    public void confirm() {
        
        answer().exec();
        
    }
    
    public void text(String text) {

        answer().text(text).exec();

    }
    
    public void alert(String alert) {

        answer().alert(alert).exec();

    }
    
    public void url(String url) {

        answer().url(url).exec();

    }
    
    public CData data = new CData(query.data());

    
    public AnswerCallback answer() {
        
        return new AnswerCallback(fragment,query.id());
        
    }
    
}
