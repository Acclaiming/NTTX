package io.kurumi.ntt.fragment.ocr;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.db.UserData;
import cn.easyproject.easyocr.EasyOCR;

public class OcrTest extends Fragment {

	@Override
	public void init(BotFragment origin) {
		
		super.init(origin);
		
		registerFunction("ocr");
		
	}
	
	final String POINT_OCR = "ocr";

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
		setPrivatePoint(user,POINT_OCR);
		
		msg.send("现在发送图片").withCancel().exec();
		
	}
	
	EasyOCR ocr = new EasyOCR("/usr/share/tessdata");

	@Override
	public void onPoint(UserData user,Msg msg,String point,Object data) {
		
		if (msg.message().photo() != null) {
	
			msg.sendTyping();
			
			msg.send(msg.photo().getPath()).exec();
			
			msg.send("结果 :",ocr.discern(msg.photo())).exec();
			
		}
		
	}
	
}
