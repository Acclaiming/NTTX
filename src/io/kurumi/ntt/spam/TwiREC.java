package io.kurumi.ntt.spam;

import io.kurumi.ntt.model.data.IdDataModel;
import cn.hutool.json.JSONObject;

public class TwiREC extends IdDataModel {

    public static Factory<TwiREC> INSTANCE = new Factory<TwiREC>(TwiREC.class, "spam/recs");

    public TwiREC(String dirName, Long id) { super(dirName, id); }
    
    public String screenName;
    public String displayName;

    @Override
    protected void init() {
    }

    @Override
    protected void load(JSONObject obj) {

      //  accountId = obj.getLong("account_id");
        screenName = obj.getStr("screen_name");
        displayName = obj.getStr("display_name");

    }

    @Override
    protected void save(JSONObject obj) {

        obj.put("account_id", id);
        
        // 记录 ？
        
        obj.put("screen_name", screenName);
        obj.put("display_name", displayName);

    }

    

}
