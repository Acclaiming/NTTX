package io.kurumi.ntt.ui.confs;

import cn.hutool.json.*;
import io.kurumi.ntt.*;

public class BoolConf extends BaseConf<Boolean> {

    public BoolConf(UserData userData,String botName,String key) {
        super(userData,botName,key);
    }
    
    @Override
    protected Boolean getFromJSONObject(JSONObject data, String key) {
        return data.getBool(key);
    }

    @Override
    protected void saveToJSONObject(JSONObject data, String key, Boolean value) {
        data.put(key,value);
    }
    
}
