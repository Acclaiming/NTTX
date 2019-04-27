package io.kurumi.ntt.funcs;

import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.funcs.abs.Function;
import io.kurumi.ntt.model.Msg;
import java.util.LinkedList;

public class Ping extends Function {

    public static Ping INSTANCE = new Ping();
    
    @Override
    public void functions(LinkedList<String> names) {
        
        names.add("ping");
        
    }

    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params) {
        
        long start = System.currentTimeMillis();

        String pong = "接收延迟 : " + ((start / 1000) - (msg.message().date())) + " ±1s";

        final Msg sended = msg.reply(pong).send();

        long end = System.currentTimeMillis();

        if (sended != null) {

            sended.edit(pong,"回复延迟 : " + (end - start) + "ms").publicFailedWith(msg);

        }
        
    }
    
}
