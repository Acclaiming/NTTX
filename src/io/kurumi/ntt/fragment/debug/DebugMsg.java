package io.kurumi.ntt.fragment.debug;

import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import cn.hutool.json.JSONObject;

public class DebugMsg extends Fragment {

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

        registerFunction("get_msg");

    }

	@Override
	public int checkFunction() {
		
		return FUNCTION_PUBLIC;
		
	}

    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params) {

        if (msg.replyTo() == null) {

            msg.send("没有对消息回复").exec();

            return;

        }

		if (msg.update != null) {

			msg.send(new JSONObject(msg.update.toString()).getJSONObject("reply_to_message").toStringPretty());

		} else {

			msg.send(msg.replyTo().message().toString()).exec();

		}

    }

}
