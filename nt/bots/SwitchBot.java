package io.kurumi.nt.bots;

import io.kurumi.nt.*;
import twitter4j.*;

public abstract class SwitchBot {

    public abstract String getBotName();
    public abstract String getConfigKey();
    
    public abstract void onInit(TwitterStream stream,TwiAccount acc);
    public abstract void onTerminated();
    
    public boolean isEnable(TwiAccount acc) {

        return acc.userData.getBool(getConfigKey(), false);

    }

    public void setEnable(TwiAccount acc, boolean enable) {

        acc.userData.put(getConfigKey(), enable);
        acc.save();

    }

}
