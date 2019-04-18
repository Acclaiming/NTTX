package io.kurumi.ntt.funcs;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.ThreadPool;
import cn.hutool.core.thread.ThreadUtil;

public class Ping extends Fragment {

    public static Ping INSTANCE = new Ping();

    @Override
    public boolean onMsg(UserData user,final Msg msg) {

        if ("ping".equals(msg.command())) {

            msg.sendTyping();

            long start = System.currentTimeMillis();

            long receive = start - (((long)msg.message().date()) * 1000);
            
            String pong = "接收延迟 : " + receive + "ms";
            
            final Msg sended = msg.reply(pong).send();

            long end = System.currentTimeMillis();

            if (sended != null) {

                sended.edit(pong,"回复延迟 : " + (end - start) + "ms").publicFailedWith(msg);
				
            }

            return true;

        }

        return false;

    }

}
