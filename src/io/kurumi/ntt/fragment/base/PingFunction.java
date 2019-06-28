package io.kurumi.ntt.fragment.base;

import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.abs.Msg;
import java.util.LinkedList;
import io.kurumi.ntt.fragment.BotFragment;

public class PingFunction extends Fragment {

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerFunction("ping");

	}

    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params) {

        long start = System.currentTimeMillis();

        long receive = ((start / 1000) - (msg.message().date()));

        String pong = "接收延迟 : " + receive + " ±1s";

        final Msg sended = msg.reply(pong).send();

        long end = System.currentTimeMillis();

        if (sended != null) {

            sended.edit(pong,"回复延迟 : " + (end - start) + "ms").publicFailedWith(msg);

        }

    }

}
