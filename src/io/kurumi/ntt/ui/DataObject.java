package io.kurumi.ntt.ui;

import cn.hutool.json.*;
import io.kurumi.ntt.serialize.*;
import java.io.*;
import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.ui.ext.*;

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
        
        new MsgExt.CallbackReply(query()).reply();
        
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
    
    public MsgExt.Send send(String... msg) {
        
        return new MsgExt.Send(msg().chat(),msg);
        
    }
    
    public MsgExt.CallbackReply reply() {

        return new MsgExt.CallbackReply(query());

    }
    
    public MsgExt.Edit edit(String... msg) {
        
        return new MsgExt.Edit(msg(),msg);
        
    }
    
    public void setPoint(String point) {
        
        put("point",point);
        
    }
    
    public String getPoint() {
        
        return getStr("point");
        
    }
    
    public void putSerializable(String key,Serializable obj) {
        
        put(key,SerUtil.toString(obj));
        
    }
    
    public <T> T getSerilizable(String key) {
        
        return SerUtil.toObject(getStr(key));
        
    }
    
}
