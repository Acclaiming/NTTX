package io.kurumi.ntt.funcs.twitter.login;

import io.kurumi.ntt.funcs.abs.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.model.*;
import java.util.*;

public class TwitterLogout extends TwitterFunction {

    public static TwitterLogout INSTANCE = new TwitterLogout();
    
    @Override
    public void functions(LinkedList<String> names) {
        
        names.add("logout");
        
    }

    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {
        
        
    }

}
