package io.kurumi.ntt.ui.confs;

import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.bots.*;
import io.kurumi.ntt.ui.*;
import io.kurumi.ntt.ui.request.*;
import java.util.concurrent.atomic.*;

public class StrConf extends BaseConf<String> {
    
    public StrConf(UserBot bot,String name,String key) {
        super(bot,name,key);
    }

    @Override
    public String get() {
   
        return bot.data.getStr(key);
        
    }

    public static final String INDEX_CHANGE = "change";

    @Override
    public void applySetting(AbsSendMsg msg) {
        
        msg.singleLineButton("设置 " + name, createQuery());
        
    }

    @Override
    public AbsResuest onCallback(DataObject obj,AtomicBoolean refresh) {
        
        bot.owner.setPoint(POINT_CONF_INPUT);
        bot.owner.save();
        
        return new SendMsg(obj.chat(),"请输入新内容 : ");
        
    }

    @Override
    public AbsResuest onMessage(Message msg,AtomicBoolean refresh) {
        
        set(msg.text());
        
        bot.owner.point = null;
        bot.owner.save();
        
        refresh.set(true);
        
        return new SendMsg(msg,"修改成功！");
        
    }

}
