package io.kurumi.ntt.taip;

import cn.xsshome.taip.nlp.*;
import cn.hutool.json.*;
import cn.hutool.core.util.*;

public class AIUtil {

    public static String id = "2111414508";
    public static String key = "e1Z3XyOYI7VuVQP0";

    public static TAipNlp nlp = new TAipNlp(id, key);

    public static float nlpTextpolar(String text) throws Exception {
        
        byte[] tb = text.getBytes(CharsetUtil.CHARSET_UTF_8);
        
        if (tb.length > 200){
            
            byte[] nb =  new byte[200];
            System.arraycopy(tb,0,nb,0,200);
            tb = nb;
            
        }
        
        JSONObject obj = new JSONObject(nlp.nlpTextpolar(StrUtil.str(tb,"utf-8"))).getJSONObject("data");

        int polar = obj.getInt("polar");
        float confd = obj.getFloat("confd");


        return polar * confd;

    }

}
