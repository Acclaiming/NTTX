package io.kurumi.ntt.ui.confs;

import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.bots.*;
import io.kurumi.ntt.ui.*;
import io.kurumi.ntt.ui.request.*;
import java.util.*;
import java.util.concurrent.atomic.*;

public abstract class BaseConf<T> {
    
    public static final String CONF_CALLBACK = "c|c";
    public static final String CONF_BACK = "c|b";
    public static final String POINT_CONF_INPUT = "c|i";
    
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

        point.put("b",bot.name);

        point.put("key",key);
        
        point.put("bk",backTo.get("k"));
        point.put("bi",backTo.get("i"));
        
        return point;

    }
    
    
    public DataObject createQuery() {
        
        DataObject obj = new DataObject();
        
        obj.setPoint(CONF_CALLBACK);
        
        obj.setBot(bot);

        obj.put("k",key);
        
        return obj;
        
    }
    
    public DataObject createBackQuery() {

        DataObject obj = new DataObject();

        obj.setPoint(CONF_BACK);
        
        obj.setBot(bot);

        obj.put("k",key);
        
        if (backTo != null) {
            
            obj.put("bk",backTo.get("k"));
            obj.put("bi",backTo.get("i"));
            
        }

        return obj;

    }
    
    public List<BaseConf> items = null;
    
    public abstract void applySetting(AbsSendMsg msg);
    
    public AbsResuest onMessage(Message msg,AtomicBoolean back) { return null; }
    
    public abstract AbsResuest onCallback(DataObject obj,AtomicBoolean refresh);
    
}
