package io.kurumi.ntt.fragment.picacg;

import io.kurumi.ntt.fragment.abs.*;
import io.kurumi.ntt.db.*;
import java.util.*;

public class SignThread extends Function {

	@Override
	public void functions(LinkedList<String> names) {

		names.add("psign");

	}

	@Override
	public void onFunction(UserData user, Msg msg, String function, String[] params) {

		if (params.length < 5) {

			msg.send("/psign name,email,pswd,birthday,gender").exec();

			return;
		}

		msg.send(PicAcgApi.register(params[0], params[1], params[2], params[3], params[4])).exec();

	}

}
