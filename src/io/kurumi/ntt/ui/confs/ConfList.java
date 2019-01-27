package io.kurumi.ntt.ui.confs;

import io.kurumi.ntt.*;
import io.kurumi.ntt.bots.*;
import io.kurumi.ntt.ui.*;
import io.kurumi.ntt.ui.request.*;
import java.util.*;
import java.util.concurrent.atomic.*;

public class ConfList extends BaseConf<List<BaseConf>> {

    public ConfList(UserBot bot,String name,String key,BaseConf... items) {
        super(bot,name,key);
        this.items = Arrays.asList(items);
    }
    
    @Override
    public List<BaseConf> get() {
        return items;
    }

    @Override
    public void set(List<BaseConf> value) {
        
        items = value;
        
   }
    
    public static final String INDEX_ENTER = "enter";

    @Override
    public void applySetting(AbsSendMsg msg) {
        
        DataObject data = createQuery();
        
        data.setindex(INDEX_ENTER);

        msg.singleLineButton(name + " >>",data);
        
    }

    @Override
    public AbsResuest onCallback(DataObject obj, AtomicBoolean refresh) {
       
        switch(obj.getIndex()) {
            
            case INDEX_ENTER : return send(obj,refresh);
            
        }
        
        return null;
    }

    private AbsResuest send(DataObject obj, AtomicBoolean refresh) {
        
        EditMsg edit = obj.edit(name);
        
        edit.singleLineButton("<< 返回上级",createBackQuery());
        
        for (BaseConf conf : items) {
            
            conf.backTo = createQuery();
            
            conf.backTo.setindex(INDEX_ENTER);
            
            conf.applySetting(edit);
            
        }

        return edit;
        
    }

    

}
