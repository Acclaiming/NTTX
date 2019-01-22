package io.kurumi.ntt.encode;

import cn.hutool.core.util.*;

public class EncTest {
    
    public static void main(String[] args) {
        
        
        
        System.out.println(Utf8String("23333".getBytes(CharsetUtil.CHARSET_UTF_8)));
        
    }
    
    public static String Utf8String(byte[] bytes) {
        
        for(byte b : bytes) {
            
            System.out.print(((Byte)b).toString());
            
        }
        
        return null;
        
    }
    
}
