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
            
            long start = System.currentTimeMillis();

            final Msg sended = msg.reply("pong！").send();

            long end = System.currentTimeMillis();

            if (sended != null) {

                sended.edit("pong！","time : " + (end - start)).exec();

            }

            ThreadPool.exec(new Runnable() {

                    @Override
                    public void run() {
                        
                        ThreadUtil.sleep(1000 * 5);
                     
                        sended.delete();
                        
                        msg.delete();
                        
                    }
                    
                });

            return true;

        }

        return false;

    }

}
