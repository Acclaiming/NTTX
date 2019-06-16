package io.kurumi.ntt.fragment.base;

import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.fragment.abs.*;
import java.util.*;

public class GetID extends Function {

	@Override
	public void functions(LinkedList<String> names) {

		names.add("id");

	}

	@Override
	public void onFunction(UserData user, Msg msg, String function, String[] params) {

		if (msg.isReply()) {

			msg.send(msg.replyTo().from().id.toString()).publicFailed();

		} else {

			msg.send(msg.chatId().toString()).publicFailed();

		}

    }

    @Override
    public boolean onChanPost(UserData user, Msg msg) {

        return onMsg(user, msg);

    }

}
