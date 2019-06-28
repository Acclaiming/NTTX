package io.kurumi.ntt.fragment.admin;

import com.pengrad.telegrambot.request.DeleteMessage;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.fragment.abs.request.Send;
import io.kurumi.ntt.fragment.BotFragment;

public class DelMsg extends Fragment {

	@Override
	public void init(BotFragment origin) {
		
		super.init(origin);
		
		registerAdminFunction("d");
		
	}
	
	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

        msg.delete();

        if (msg.isReply()) {

            msg.replyTo().delete();

        }

    }


}
