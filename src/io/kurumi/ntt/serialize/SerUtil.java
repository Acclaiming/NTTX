package io.kurumi.ntt.serialize;

import java.io.*;
import org.apache.commons.codec.binary.*;
import cn.hutool.core.util.*;

public class SerUtil {
    
    public static <T> T toObject(String str) {

        ByteArrayInputStream bytes = new ByteArrayInputStream(Base64.decodeBase64(str));

        try {

            ObjectInputStream in = new ObjectInputStream(bytes);

            T obj =  (T) in.readObject();
            
            in.close();
            
            return obj;

        } catch (Exception e) {}

        return null;

    }
    
    public static String toString(Serializable obj) {
        
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        try {
            
            ObjectOutputStream out = new ObjectOutputStream(bytes);
            
            out.writeObject(obj);
            
            out.close();
            
            return Base64.encodeBase64String(bytes.toByteArray());

        } catch (IOException e) {}
        
        return null;

    }
    
}
