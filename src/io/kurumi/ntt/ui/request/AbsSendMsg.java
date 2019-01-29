package io.kurumi.ntt.ui.request;

import com.pengrad.telegrambot.response.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.ui.*;
import io.kurumi.ntt.ui.model.*;
import java.util.*;

public abstract class AbsSendMsg implements AbsResuest {
    
    public void processObject(DataObject obj) {}
    
    public abstract AbsSendMsg html();
    public abstract AbsSendMsg markdown();
    public abstract AbsSendMsg disableWebPagePreview();
    public abstract AbsSendMsg disableNotification();
    
    public abstract BaseResponse exec();
    
    protected LinkedList<InlineButtonGroup> inlineKeyBoardGroups = new LinkedList<>();
    
    public InlineButtonGroup newInlineButtonGroup() {
        
        InlineButtonGroup markup = new InlineButtonGroup(this);
        
        inlineKeyBoardGroups.add(markup);
        
        return markup;

    }
    
    public void singleLineOpenUrlButton(String text,String url) {

        newInlineButtonGroup().newOpenUrlButton(text,url);

    }
    
    public void singleLineButton(String text,DataObject obj) {
        
        newInlineButtonGroup().newButton(text,obj);
     
    }
    
    public void singleLineButton(String text,String point) {
        
        newInlineButtonGroup().newButton(text,point);

    }
    
    public void singleLineButton(String text,String point,TwiAccount acc) {

        newInlineButtonGroup().newButton(text,point,acc);

    }
    
    public void singleLineButton(String text,String point,String index) {

        newInlineButtonGroup().newButton(text,point,index);

    }
    
    
}
