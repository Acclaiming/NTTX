package io.kurumi.ntt;

import io.kurumi.ntt.ui.request.*;
import io.kurumi.ntt.ui.*;

public interface UserTask extends Runnable {
    
    public abstract String taskName();
    
    public abstract void applySetting(UserData userData,AbsSendMsg msg);
    
    public abstract void onCallback(UserData userData, DataObject obj);
    
}
