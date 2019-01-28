package io.kurumi.ntt.ui.confs;

import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.bots.*;
import io.kurumi.ntt.ui.*;
import io.kurumi.ntt.ui.request.*;
import java.util.*;
import java.util.concurrent.atomic.*;

public abstract class BaseConf<T> {
    
    public static final String CONF_CALLBACK = "conf|callback";
    public static final String CONF_BACK = "conf|back";
    public static final String POINT_CONF_INPUT = "conf|input";
    
    public String name;
    public UserBot bot;
    public String key;

    public DataObject backTo;

    public BaseConf(UserBot bot,String name,String key) {
        this.name = name;
        this.bot = bot;
        this.key = key;
    }
    
    public abstract T get();
    
    public void set(T value) {
        
        bot.data.put(key,value);
        
    }
    
    public DataObject createInputPoint() {

        DataObject point = new DataObject();

        point.setPoint(POINT_CONF_INPUT);

        point.put("bn",bot.name);

        point.put("key",key);
        
        point.put("back",createBackQuery());

        return point;

    }
    
    
    public DataObject createQuery() {
        
        DataObject obj = new DataObject();
        
        obj.setPoint(CONF_CALLBACK);
        
        obj.put("bn",bot.name);

        obj.put("key",key);
        
        return obj;
        
    }
    
    public DataObject createBackQuery() {

        DataObject obj = new DataObject();

        obj.setPoint(CONF_BACK);
        
        obj.put("bn",bot.name);

        obj.put("key",key);
        
        if (backTo != null) {
            
            obj.put("backTo",backTo);
            
        }

        return obj;

    }
    
    public List<BaseConf> items = null;
    
    public abstract void applySetting(AbsSendMsg msg);
    
    public AbsResuest onMessage(Message msg,AtomicBoolean back) { return null; }
    
    public abstract AbsResuest onCallback(DataObject obj,AtomicBoolean refresh);
    
}
