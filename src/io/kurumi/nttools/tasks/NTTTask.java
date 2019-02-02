package io.kurumi.nttools.tasks;

import io.kurumi.nttools.model.Callback;
import io.kurumi.nttools.model.Msg;
import io.kurumi.nttools.utils.UserData;
import java.util.LinkedList;
import java.util.List;
import cn.hutool.json.JSONObject;

public abstract class NTTTask {
    
    public final UserData user;
    public final String name;
    public JSONObject data;

    public NTTTask(UserData user,String name) {
        
        this.user = user;
        this.name = name;
        
        load();
        
    }
    
    public abstract String type();
   
    public abstract boolean status();
    
    public abstract String enableString();
    public abstract String disableString();
    
    public abstract void process(Msg msg);
    public abstract void callback(Callback callback);

    public abstract void start(UserData user,Callback callback);

    public abstract void startAtBackground();

    public abstract void interrupt();
    
    public void load() {
        
        data = user.getByPath("user_tasks." + name,JSONObject.class);

        if (data == null) {
            
            data = new JSONObject();
            
        }
        
    }
    
    public void save() {
        
        user.putByPath("user_tasks." + name,data);
        
        user.save();
    }
    
}
