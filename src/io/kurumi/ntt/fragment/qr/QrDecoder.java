package io.kurumi.ntt.fragment.qr;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;
public class QrDecoder extends Fragment {

	@Override
	public void init(BotFragment origin) {
		
		super.init(origin);
		
		registerFunction("qr_decode");
		
	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
		if (!msg.isReply() || msg.replyTo().message().photo() == null) {
			
			msg.send("请对图片回复 :)").async();
			
			return;
			
		}
		
		new MultiFormatReader();
		
	}
	
}
