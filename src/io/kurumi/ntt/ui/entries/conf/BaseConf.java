package io.kurumi.ntt.ui.entries.conf;

import io.kurumi.ntt.*;
import org.json.*;

public class BaseConf<T> {
    
    public final UserData userData;
    public final String key;

    public JSONObject data;
    
    public BaseConf(UserData userData,String key) {
        this.userData = userData;
        this.key = key;
    }
    
}
