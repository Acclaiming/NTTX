package io.kurumi.ntt.fragment.twitter.login;

import io.kurumi.ntt.Env;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.fragment.abs.TwitterFunction;
import io.kurumi.ntt.fragment.abs.request.Send;
import io.kurumi.ntt.fragment.twitter.TAuth;
import java.util.LinkedList;

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
