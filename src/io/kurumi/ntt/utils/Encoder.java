package io.kurumi.ntt.utils;

import java.util.*;

public class Encoder {

    public static final String[] coreValus = {"富强","民主","文明","和谐","自由","平等","公正","法治","爱国","敬业","诚信","友善"};
    
    private HashMap<Integer,LinkedList<String>> list = new HashMap<>();
    private HashMap<String,Integer> resl = new HashMap<>();
    
    private LinkedList<String> split = new LinkedList<>();

    public Encoder(String[] hash) {

        int index = 0;

        for (String kw : hash) {

            if (index == 10) {

                split.add(kw);

                index = 0;

            } else {
                
                LinkedList<String> kl = list.get(index);

                if (kl == null) kl = new LinkedList<>();
                
                kl.add(kw);

                list.put(index,kl);
                
                resl.put(kw,index);

                index ++;

            }

        }

    }
   
    private Random random = new Random();
    
    public String encode(String content) {
        
        StringBuilder str = new StringBuilder();
        
        for (int index = 0;index < content.length();index ++) {
            
            String cs = ((Long)((long)content.charAt(index))).toString();
            
            for (int ci = 0;ci < cs.length();ci ++) {

                int kwi = Integer.parseInt(cs.substring(ci,ci + 1));
                
                LinkedList<String> kwl = list.get(kwi);
                
                str.append(kwl.get(random.nextInt(kwl.size())));

            }
            
            str.append(split.get(random.nextInt(split.size())));
            
        }
        
        return str.toString();
        
    }
    
    public String decode(String hash) {
        
        try {
        
        StringBuilder str = new StringBuilder();

        StringBuilder cache = new StringBuilder();
        
        for (int index = 0;index < hash.length();index  = index + 2) {

            String kw = hash.substring(index,index + 2);

            if (resl.containsKey(kw)) {
                
                cache.append(resl.get(kw));
                
            } else if (split.contains(kw)) {
                
                str.append(((char)Long.parseLong(cache.toString())));
                
                cache = new StringBuilder();
                
            } else return null;

        }

        return str.toString();
        
        } catch (Exception e) {}
        
        return null;
        
    }

}
