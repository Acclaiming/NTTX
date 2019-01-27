package io.kurumi.ntt.ui.confs;

import io.kurumi.ntt.*;
import io.kurumi.ntt.bots.*;
import io.kurumi.ntt.ui.*;
import io.kurumi.ntt.ui.request.*;
import java.util.concurrent.atomic.*;

public class BoolConf extends BaseConf<Boolean> {
    
    public BoolConf(UserBot bot,String name,String key) {
        super(bot,name,key);
    }

    @Override
    public Boolean get() {
        
       return bot.data.getBool(key);
       
    }

    @Override
    public void applySetting(AbsSendMsg msg) {
        
        msg.singleLineButton(name + " : 「" + getSwitchMsg() + "」",createQuery());
        
    }

    @Override
    public AbsResuest onCallback(DataObject obj,AtomicBoolean refresh) {
        
        refresh.set(true);
        
        set(!get());
        
        return obj.reply().text("修改成功 ~");
        
    }
    
    private String getSwitchMsg() {
        
        if (get()) {
            
            return "开";
            
        } else {
        
            return "关";
        
        }
        
    }

}
