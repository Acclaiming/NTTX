package io.kurumi.nttools.utils;

import cn.hutool.json.JSONObject;
import io.kurumi.nttools.twitter.TwiAccount;
import java.io.Serializable;

public class CData extends JSONObject {

    public CData() {
        super();
    }

    public CData(String json) {
        super(json);
    }
    
    public CData(JSONObject json) {
        super(json);
    }

    public void setPoint(String point) {

        put("p",point);

    }

    public String getPoint() {

        return getStr("p");

    }

    public void setindex(String point) {

        put("i",point);

    }

    public String getIndex() {

        return getStr("i");

    }

    public void setUser(UserData u,TwiAccount account) {

        put("a",u.twitterAccounts.indexOf(account));

    }

    public TwiAccount getUser(UserData userData) {

        return userData.twitterAccounts.get(getInt("a"));

    }
    
    public CData getData(String key) {
        
        return new CData(getJSONObject(key));
        
    }

    public void putSerializable(String key,Serializable obj) {

        put(key,SerUtil.toString(obj));

    }

    public <T> T getSerilizable(String key) {

        return SerUtil.toObject(getStr(key));

    }

}

