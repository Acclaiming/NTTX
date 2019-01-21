package io.kurumi.ntbot.markdown;

import org.pegdown.PegDownProcessor;

public class MD {
    
    public static PegDownProcessor processer =  new PegDownProcessor();
    
    public static String toHtml(String content) {
        
        return processer.markdownToHtml(content);
        
    }
    
}
