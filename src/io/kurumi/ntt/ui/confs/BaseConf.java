package io.kurumi.ntt.ui.confs;

import io.kurumi.ntt.*;
import cn.hutool.json.*;
import io.kurumi.ntt.ui.request.*;

public abstract class BaseConf<T> {
    
    public UserData userData;
    private String botName;
    private String key;

    public BaseConf(UserData userData,String botName,String key) {
        this.userData = userData;
        this.botName = botName;
        this.key = key;
    }
    
    public T get() {
        
        JSONObject data = userData.ext.getJSONObject(botName);
        
        if (data == null) data = new JSONObject();
        
        return getFromJSONObject(data,key);
        
    }
    
    public void set(T value) {
        
        JSONObject data = userData.ext.getJSONObject(botName);

        if (data == null) data = new JSONObject();

        saveToJSONObject(data,key,value);
        
        userData.ext.put(botName,data);
        
        userData.save();
        
    }
    
    protected abstract T getFromJSONObject(JSONObject data,String key);
    protected abstract void saveToJSONObject(JSONObject data,String key,T value);

    
}
