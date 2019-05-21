package io.kurumi.ntt.fragment.base;

import io.kurumi.ntt.funcs.abs.*;
import java.util.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.db.*;

public class PingFunction extends Function {

	@Override
	public void functions(LinkedList<String> names) {
		
        names.add("ping");

    }

    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params) {

        long start = System.currentTimeMillis();

		long receive =  ((start / 1000) - (msg.message().date()));
		
        String pong = "接收延迟 : " + receive + " ±1s";

        final Msg sended = msg.reply(pong).send();

        long end = System.currentTimeMillis();

        if (sended != null) {

            sended.edit(pong,"回复延迟 : " + (end - start) + "ms").publicFailedWith(msg);

        }
		
	}

}
