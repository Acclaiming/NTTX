package io.kurumi.ntt.fragment.debug;

import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.abs.Msg;
import java.util.LinkedList;
import io.kurumi.ntt.fragment.BotFragment;

public class DebugMsg extends Fragment {

	@Override
	public void init(BotFragment origin) {
		
		super.init(origin);
		
        registerFunction("get_msg");

    }

    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params) {

        if (msg.replyTo() == null) {

            msg.send("没有对消息回复").exec();

            return;

        }

        msg.send(msg.replyTo().message().toString()).exec();

    }

}
