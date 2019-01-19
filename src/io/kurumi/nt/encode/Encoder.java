package io.kurumi.nt.encode;

import java.util.*;

public class Encoder {
    
    public static final String[] coreValus = {"富强","民主","文明","和谐","自由","平等","公正","法治","爱国","敬业","诚信","友善"};
    
    private LinkedList<String> list = new LinkedList<>();
    private LinkedList<String> split = new LinkedList<>();
    
    public Encoder(String[] hash) {
        
        int index = 0;
        
        for(String kw : hash) {
            
            if (index == 11) {
                
                index = 0;
                
            }
            
            if (index == 0) {
                
                split.add(kw);
                
                index ++;
                
            } else {
                
                list.add(kw);
                
            }
            
        }
        
    }
    
}
