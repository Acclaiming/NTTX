 package io.kurumi.nttools.utils;

import cn.hutool.http.*;
import java.util.*;
import cn.hutool.json.*;

public class NaiveBoom {
    
    public static String read(String guid) {
        
        return read("https://naive.cf",guid);
        
    }
    
    public static String read(String site,String guid) {
        
        LinkedHashMap<String,Object> params = new LinkedHashMap<>();

        params.put("guid",guid);

        String json =  HttpUtil.post(site + "/apis/get-msg", params);
        
        JSONObject obj = new JSONObject(json);
        
        if (obj.getInt("status",0) == 1) {
            
            return obj.getStr("text",null);
            
        }
        
        return null;
        
    }
    
    public static String create(String text) {
        
        return create("https://native.cf",text);
        
    }
    
    public static String create(String site,String text) {
        
        LinkedHashMap<String,Object> params = new LinkedHashMap<>();

        params.put("text",text);
        
        return HttpUtil.post(site + "/apis/get-temp", params);
        
    }
    
}
