package io.kurumi.ntt.funcs.twitter.track;

import io.kurumi.ntt.funcs.abs.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.model.*;
import java.util.*;

public class FollowersTrack extends TwitterFunction {

    @Override
    public void functions(LinkedList<String> names) {
    
        names.add("ft");
        names.add("ftoff");
        
    }

    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {
        
        if (!function.endsWith("off")) {
            
            
            
        }
        
    }

}
