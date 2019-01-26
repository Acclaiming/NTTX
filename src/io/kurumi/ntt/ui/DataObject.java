package io.kurumi.ntt.ui;

import cn.hutool.json.*;
import io.kurumi.ntt.serialize.*;
import java.io.*;
import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.ui.ext.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.ui.request.*;

public class DataObject extends JSONObject {
    
    private CallbackQuery query;
    
    public DataObject() {
        super();
    }
    
    public DataObject(CallbackQuery query) {
        super(query.data());
        this.query = query;
    }
    
    public void confirmQuery() {
        
        new AnswerCallback(query()).exec();
        
    }
    
    public Message msg() {
        
        return query().message();
        
    }
    
    public Chat chat() {
        
        return msg().chat();
        
    }
    
    public String text() {
        
        return msg().text();
        
    }
    
    public void deleteMsg() {
        
        MsgExt.delete(msg());
        
    }
    
    public CallbackQuery query() {
        
        return query;
        
    }
    
    public SendMsg send(String... msg) {
        
        return new SendMsg(msg().chat(),msg);
        
    }
    
    public AnswerCallback reply() {

        return new AnswerCallback(query());

    }
    
    public EditMsg edit(String... msg) {
        
        return new EditMsg(msg(),msg);
        
    }
    
    public void setPoint(String point) {
        
        put("point",point);
        
    }
    
    public String getPoint() {
        
        return getStr("point");
        
    }
    
    public void setUser(TwiAccount account) {
        
        put("accountId",account);
        
    }
    
    public TwiAccount getUser(UserData UserData) {
        
        return UserData.find(getLong("accountId",-1L));
        
    }
    
    public void putSerializable(String key,Serializable obj) {
        
        put(key,SerUtil.toString(obj));
        
    }
    
    public <T> T getSerilizable(String key) {
        
        return SerUtil.toObject(getStr(key));
        
    }
    
}
