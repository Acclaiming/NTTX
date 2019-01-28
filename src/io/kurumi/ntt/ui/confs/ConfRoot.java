package io.kurumi.ntt.ui.confs;

import io.kurumi.ntt.*;
import io.kurumi.ntt.ui.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import io.kurumi.ntt.ui.request.*;
import io.kurumi.ntt.bots.*;
import com.pengrad.telegrambot.model.*;

// conf root for a bot

public abstract class ConfRoot extends LinkedList<BaseConf> {

    public ConfRoot(UserBot bot) {

        bot.confs(this);

    }

    public abstract void refresh(DataObject obj);
  
    public AbsResuest onCallback(UserData userData, DataObject obj) {

        switch (obj.getPoint()) {

                case BaseConf.CONF_CALLBACK : return onItemCallback(obj);

        }

        return obj.reply().alert("非法的设置回调指针 : " + obj.getPoint());

    }

    public AbsResuest processInput(UserData userData, Message msg) {

        BaseConf target = getTargetConf(userData.point.getStr("k"), null);

        AtomicBoolean back = new AtomicBoolean(false);

        DataObject backObj = new DataObject();
        
        backObj.setPoint(BaseConf.CONF_CALLBACK);
        
        String key = userData.point.getStr("bk");
        String index = userData.point.getStr("bi");
        
        backObj.put("k",key);
        backObj.setindex(index);
        
        backObj.msg = msg;
        

        try {

            return target.onMessage(msg, back);

        } finally {

            if (back.get()) {

                refresh(backObj);

            }

        }

    }

    private AbsResuest onItemCallback(DataObject obj) {

        BaseConf target = getTargetConf(obj.getStr("k"), null);

        AtomicBoolean refresh = new AtomicBoolean(false);

        try {

            return target.onCallback(obj, refresh);


        } finally {

            if (refresh.get()) {

                if (target.backTo == null) {

                    refresh(obj);

                } else {
                    
                    target.backTo.msg = obj.msg;

                    refresh(target.backTo);

                }

            }


        }

    }

    public void applySettings(AbsSendMsg obj) {

        for (BaseConf item : this) {

            item.applySetting(obj);

        }

    }

    private BaseConf getTargetConf(String key, List<BaseConf> items) {

        if (items == null) items = this;


        for (BaseConf conf : this) {

            if (key.equals(conf.key)) return conf;

            if (conf.items != null) {

                BaseConf item = getTargetConf(key, conf.items);

                if (item != null) return item;

            }

        }

        return null;

    }

}
