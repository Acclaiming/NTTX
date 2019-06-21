package io.kurumi.ntt.fragment.voice;

import io.kurumi.ntt.fragment.abs.Function;
import java.util.LinkedList;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.db.UserData;
import com.techventus.server.voice.Voice;
import java.io.IOException;

public class GoogleVoice extends Function {

    @Override
    public void functions(LinkedList<String> names) {
       
        names.add("gv");
        
    }

    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params) {
       
        if (params.length < 2) {
            
            msg.send("/gv email pswd").exec();
            
            return;
            
        }
        
        try {
            
            msg.send("登录成功 : " ,new Voice(params[0], params[1]).getAuthToken()).exec();
            
        } catch (IOException e) {
            
            msg.send(e.toString()).exec();
            
        }

    }

}
