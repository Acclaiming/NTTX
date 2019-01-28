package io.kurumi.ntt.ui;

import cn.hutool.json.*;
import io.kurumi.ntt.serialize.*;
import java.io.*;
import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.ui.ext.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.ui.request.*;
import io.kurumi.ntt.bots.*;
import cn.hutool.core.util.*;

public class DataObject extends JSONObject {
    
    public CallbackQuery query;
    public Message msg;
    
    public DataObject() {
        super();
    }
    
    public DataObject(String json) {
        super(json);
    }
    
    public DataObject(JSONObject json) {
        super(json);
    }
    
    public DataObject(CallbackQuery query) {
        super(query.data());
        this.query = query;
        this.msg = query.message();
    }
    
    public void confirmQuery() {
        
        new AnswerCallback(query()).exec();
        
    }
    
    public Message msg() {
        
        return msg;
        
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
    
    public SendMsg reply(String... msg) {

        return new SendMsg(msg(),msg);

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
    
    public void setindex(String point) {

        put("index",point);

    }

    public String getIndex() {

        return getStr("index");

    }
    
    public void setBot(UserBot bot) {

        put("bn",bot.name);

    }

    public UserBot getBot(UserData userData) {

        return userData.findBot(getStr("bn"));

    }
    
    public void setUser(TwiAccount account) {
        
        put("accountId",account.accountId);
        
    }
    
    public TwiAccount getUser(UserData UserData) {
        
        return UserData.findUser(getLong("accountId",-1L));
        
    }
    
    public DataObject getData(String key) {

        return new DataObject(getJSONObject(key));

    }
    
    
    public void putSerializable(String key,Serializable obj) {
        
        put(key,SerUtil.toString(obj));
        
    }
    
    public <T> T getSerilizable(String key) {
        
        return SerUtil.toObject(getStr(key));
        
    }

}
