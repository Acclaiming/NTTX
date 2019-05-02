package io.kurumi.ntt.utils;

import cn.hutool.core.net.URLEncoder;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import cn.hutool.core.util.CharsetUtil;

public class TentcentNlp {

    //分词    GBK
    public static final String NLP_WORDSEG = "https://api.ai.qq.com/fcgi-bin/nlp/nlp_wordseg";
    //词性标注 GBK
    public static final String NLP_WORDPOS = "https://api.ai.qq.com/fcgi-bin/nlp/nlp_wordpos";
    //专有名词识别  GBK
    public static final String NLP_WORDNER = "https://api.ai.qq.com/fcgi-bin/nlp/nlp_wordner";
    //同义词识别  GBK
    public static final String NLP_WORDSYN = "https://api.ai.qq.com/fcgi-bin/nlp/nlp_wordsyn";
    //意图成分识别  UTF-8
    static final String NLP_WORDCOM = "https://api.ai.qq.com/fcgi-bin/nlp/nlp_wordcom";
    //情感分析识别  UTF-8
    static final String NLP_TEXTPOLAR = "https://api.ai.qq.com/fcgi-bin/nlp/nlp_textpolar";
    //基础闲聊  UTF-8        
    static final String NLP_TEXTCHAT = "https://api.ai.qq.com/fcgi-bin/nlp/nlp_textchat";
    //文本翻译（AI Lab） UTF-8
    static final String NLP_TEXTTRANS = "https://api.ai.qq.com/fcgi-bin/nlp/nlp_texttrans";
    //文本翻译（翻译君） UTF-8
    static final String NLP_TEXTTRANSLATE = "https://api.ai.qq.com/fcgi-bin/nlp/nlp_texttranslate";
    //图片翻译 UTF-8
    static final String NLP_IMAGETRANSLATE = "https://api.ai.qq.com/fcgi-bin/nlp/nlp_imagetranslate";   
    //语音翻译 UTF-8
    static final String NLP_SPEECHTRANSLATE = "https://api.ai.qq.com/fcgi-bin/nlp/nlp_speechtranslate"; 
    //语种识别 UTF-8
    static final String NLP_TEXTDETECT = "https://api.ai.qq.com/fcgi-bin/nlp/nlp_textdetect";


    static final String APP_ID = "2111414508";
    static final String APP_KEY = "e1Z3XyOYI7VuVQP0";

    public static float nlpTextpolar(String text) {

        HttpRequest request = HttpUtil.createPost(NLP_TEXTPOLAR);

        request.form("app_id",APP_ID);
        request.form("time_stamp",System.currentTimeMillis() / 1000 + "");
        request.form("nonce_str",getRandomString());
        request.form("text",text);

        String sign = getSignature(request.form(),APP_KEY);

        request.form("sign",sign);

        try {

            HttpResponse resp = request.execute();

            BotLog.debug(resp.toString());
            
            if (!resp.isOk()) return 0;

            JSONObject data = new JSONObject(resp.body());

            if (data.getInt("ret") != 0) return 0;

            return data.getFloat("polar") * data.getFloat("confd");

        } catch (Exception e) {}

        return 0;

    }
    
    public static String nlpTextchat(Long chatId,String text)  {

        HttpRequest request = HttpUtil.createPost(NLP_TEXTCHAT);

        request.form("app_id",APP_ID);
        request.form("time_stamp",System.currentTimeMillis() / 1000 + "");
        request.form("nonce_str",getRandomString());
        
        request.form("session",chatId.toString());
        request.form("question", text);
        
        String sign = getSignature(request.form(),APP_KEY);
     
        request.form("sign",sign);
        
        try {

            HttpResponse resp = request.execute();

            BotLog.debug(resp.toString());
            
            if (!resp.isOk()) return null;

            JSONObject data = new JSONObject(resp.body());

            if (data.getInt("ret") != 0) return null;

            return data.getByPath("data.answer",String.class);

        } catch (Exception e) {}

        return null;

    }
    
    public static String getRandomString(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";     
        Random random = new Random();     
        StringBuffer sb = new StringBuffer();     
        for (int i = 0; i < length; i++) {     
            int number = random.nextInt(base.length());     
            sb.append(base.charAt(number));     
        }     
        return sb.toString();     
    }

    public static String getRandomString() {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";     
        Random random = new Random();     
        StringBuffer sb = new StringBuffer();     
        for (int i = 0; i < 10; i++) {     
            int number = random.nextInt(base.length());     
            sb.append(base.charAt(number));     
        }     
        return sb.toString();     
    }

    public static String md5(String s) {  
        char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};         
        try {  
            byte[] btInput = s.getBytes();  
            // 获得MD5摘要算法的 MessageDigest 对象  
            MessageDigest mdInst = MessageDigest.getInstance("MD5");  
            // 使用指定的字节更新摘要  
            mdInst.update(btInput);  
            // 获得密文  
            byte[] md = mdInst.digest();  
            // 把密文转换成十六进制的字符串形式  
            int j = md.length;  
            char str[] = new char[j * 2];  
            int k = 0;  
            for (int i = 0; i < j; i++) {  
                byte byte0 = md[i];  
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];  
                str[k++] = hexDigits[byte0 & 0xf];  
            }  
            return new String(str);  
        } catch (Exception e) {  
            e.printStackTrace();  
            return null;  
        }  
    }

    static URLEncoder encoder = new URLEncoder();
    
    public static String getSignature(Map<String,Object> params,String app_key) {
        // 先将参数以其参数名的字典序升序进行排序
        Map<String, Object> sortedParams = new TreeMap<>(params);
        Set<Map.Entry<String, Object>> entrys = sortedParams.entrySet();
        // 遍历排序后的字典，将所有参数按"key=value"格式拼接在一起
        StringBuilder baseString = new StringBuilder();
        for (Map.Entry<String, Object> param : entrys) {
            //sign参数 和 空值参数 不加入算法
            if (param.getValue() != null && !"".equals(param.getKey().trim()) && !"sign".equals(param.getKey().trim()) && !"".equals(param.getValue().toString().trim())) {
                baseString.append(param.getKey().trim()).append("=").append(encoder.encode(param.getValue().toString().trim(),CharsetUtil.CHARSET_UTF_8)).append("&");
            }
        }
        if (baseString.length() > 0) {
            baseString.deleteCharAt(baseString.length() - 1).append("&app_key=" + app_key);
        }
        // 使用MD5对待签名串求签
        try {
            String sign = md5(baseString.toString());
            return sign;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
