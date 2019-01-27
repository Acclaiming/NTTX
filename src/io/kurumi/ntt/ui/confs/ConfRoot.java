package io.kurumi.ntt.ui.confs;

import io.kurumi.ntt.*;
import io.kurumi.ntt.ui.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import io.kurumi.ntt.ui.request.*;

// conf root for a bot

public class ConfRoot extends LinkedList<BaseConf> {

    public ConfRoot() {}
    
    public void onCallback(UserData userData, DataObject obj) {

        switch (obj.getPoint()) {

                case BaseConf.CONF_BACK : onItemCallback(obj);

        }

    }

    private void onItemCallback(DataObject obj) {

        BaseConf target = getTargetConf(obj.getStr("key"), null);

        AtomicBoolean refresh = new AtomicBoolean(false);

        try {

            target.onCallback(obj, refresh);


        } finally {

            if (refresh.get()) {

                if (target.backTo == null) {

                    

                } else {
                    
                    onItemCallback(target.backTo);
                    
                }

            }


        }

    }

    private void applySettings(AbsSendMsg obj) {

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
