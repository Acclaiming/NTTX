package io.kurumi.ntt.fragment.debug;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;
import com.pengrad.telegrambot.request.SendPhoto;
import io.kurumi.ntt.utils.Img;
import cn.hutool.core.util.ArrayUtil;

public class AwtTest extends Fragment {

	@Override
	public void init(BotFragment origin) {
		
		super.init(origin);
		
		registerFunction("awt");
		
	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
		Img img = new Img(500,800);
		
		if (params.length != 0) {
			
			img.font(ArrayUtil.join(params," "));
			
		}
		
		img.fontSize(39);
		
		img.drawRandomColorTextCenter(0,0,0,0,"喵");
	
			execute(new SendPhoto(msg.chatId(),img.getBytes()));
		
	}
	
}
