package io.kurumi.ntt.bots;

import cn.hutool.json.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.bots.template.*;
import io.kurumi.ntt.ui.*;
import io.kurumi.ntt.ui.confs.*;
import io.kurumi.ntt.ui.request.*;
import java.util.*;

public abstract class UserBot {
    
    public UserData owner;
    
    public String name;

    public JSONObject data = new JSONObject();
    
    public ConfRoot root;
    
    public boolean enable = false;
 
    public UserBot(UserData owner,String name) {
        
        this.owner = owner;
        this.name = name;
        
        LinkedList<UserBot> bots = BotControl.bots.getOrDefault(owner,new LinkedList<UserBot>());
        
        bots.add(this);
        
        BotControl.bots.put(owner,bots);
        
    }

    public abstract String type();
    public abstract void confs(ConfRoot confs);

    public abstract void startAtBackground();
    public abstract AbsResuest start(DataObject obj);
    
    public void interrupt() {
        
        BotControl.bots.get(owner).remove(this);
        
    }
    
    public JSONObject toJSONObject() {
        
        return new JSONObject()
        .put("name",name)
        .put("enable",enable)
        .put("type",type())
        .put("data",data);
        
    }
    
    public static <T extends UserBot> T create(String type,UserData userData,String name) {
        
        switch(type) {

                case SeeYouNextTimeBot.TYPE : return (T)new SeeYouNextTimeBot(userData,name);
                case TwitterReFoBot.TYPE : return (T)new TwitterReFoBot(userData,name);
                
        }
        
        return null;
        
    }
    
    public static <T extends UserBot> T fromJSONObject(UserData userData,JSONObject obj) {
       
        String name = obj.getStr("name");
 
        T bot = null;
        
        bot = create(obj.getStr("type"),userData,name);
        
        bot.enable = obj.getBool("enable",false);
        bot.data = obj.getJSONObject("data");
        
        return bot;
        
    }
   
}
