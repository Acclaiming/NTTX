package io.kurumi.ntt.ui.entries;
import io.kurumi.ntt.*;
import io.kurumi.ntt.ui.*;
import io.kurumi.ntt.ui.request.*;

public class Bots {
    
    public static final String MAIN = "bots|main";
    
    public static AbsResuest main(UserData userData,DataObject obj) {
        
        return new EditMsg(obj.msg(),"Bot菜单！") {{
            
            
            
        }};
        
    }
    
}
