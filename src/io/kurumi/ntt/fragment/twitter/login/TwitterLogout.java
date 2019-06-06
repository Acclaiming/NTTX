package io.kurumi.ntt.fragment.twitter.login;

import io.kurumi.ntt.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.funcs.abs.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.model.request.*;
import io.kurumi.ntt.twitter.*;
import java.util.*;

public class TwitterLogout extends TwitterFunction {

    public static TwitterLogout INSTANCE = new TwitterLogout();
    
    @Override
    public void functions(LinkedList<String> names) {
        
        names.add("logout");
        
    }

    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {
        
        TAuth.data.deleteById(account.id);
        
        msg.send("乃的授权 " + account.archive().urlHtml() + "已移除 ~").html().exec();
        
		new Send(Env.GROUP,"Removed Auth : " + user.userName() + " -> " + account.archive().urlHtml()).html().exec();
		
    }

}
