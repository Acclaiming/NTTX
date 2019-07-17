package io.kurumi.ntt.fragment.base;

import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;

public class PingFunction extends Fragment {

		@Override
		public void init(BotFragment origin) {

				super.init(origin);

				registerFunction("ping");

		}

		@Override
		public int checkFunctionContext(UserData user,Msg msg,String function,String[] params) {

				return FUNCTION_PUBLIC;

		}

    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params) {

				msg.reply("pong").async();

    }

}
