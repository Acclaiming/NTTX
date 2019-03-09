package io.kurumi.ntt.spam;

import cn.hutool.json.*;
import io.kurumi.ntt.model.data.*;
import java.util.*;

public class SpamTag extends AIIdDataModel {

    public static Factory<SpamTag> INSTANCE = new Factory<SpamTag>(SpamTag.class,"spam/tags");
    
    public SpamTag(String dirName) { super(dirName); }
    public SpamTag(String dirName,long id) { super(dirName,id); }
    
    public String name;
    public String desc;
    public LinkedList<Long> enable;
   
    public Long tid;

    @Override
    protected void init() {
        
        desc = "暂无";
        
        enable = new LinkedList<>();
        
    }

    @Override
    protected void load(JSONObject obj) {
        
        name = obj.getStr("name");

        desc = obj.getStr("desc",desc);
        
        tid = obj.getLong("tid");
        
        JSONArray enableArray = obj.getJSONArray("enable");

        for (int index = 0; index < enableArray.size(); index++) {

            enableArray.add(enableArray.getLong(index));

        }
       
    }

    @Override
    protected void save(JSONObject obj) {
        
        obj.put("name",name);
        obj.put("desc",desc);
        obj.put("tid",tid);
        obj.put("enable",enable);
        
    }

    

}
