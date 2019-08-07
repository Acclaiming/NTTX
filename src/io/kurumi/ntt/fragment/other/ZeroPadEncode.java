package io.kurumi.ntt.fragment.other;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;
import cn.hutool.core.util.ArrayUtil;
import io.kurumi.ntt.db.PointData;
import io.kurumi.ntt.utils.ZeroPad;
import com.pengrad.telegrambot.request.SendDocument;
import cn.hutool.core.util.StrUtil;

public class ZeroPadEncode extends Fragment {

	@Override
	public void init(BotFragment origin) {
		
		super.init(origin);
		
		registerFunction("zp_encode","zp_decode");
		
		registerPoint(POINT_ENCODE_ZREOPAD);
		
	}
	
	final String POINT_ENCODE_ZREOPAD = "zp_encode";
	
	class StringEncode extends PointData {
		
		String text;
		
	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
		if (function.endsWith("encode")) {
			
			PointData data = setPrivatePoint(user,POINT_ENCODE_ZREOPAD,new StringEncode().with(msg));

			msg.send("现在输入正常显示的字符 :").exec(data);
			
		} else {
			
			String decoded = ZeroPad.decodeFrom(msg.text());
			
			if (decoded.isEmpty()) {
				
				msg.send("没有解析出零宽度字符水印。").async();
				
				return;
				
			} 
			
			msg.send(decoded).async();
			
		}
		
	}

	@Override
	public void onPoint(UserData user,Msg msg,String point,PointData data) {
		
		if (!msg.hasText()) {
			
			msg.send("正在进行零宽字符编码 ，请输入字符 :").exec(data);
			
			return;
			
		}
		
		StringEncode pad = (StringEncode) data.with(msg);
		
		if (data.step == 0) {
			
			pad.text = msg.text();
			
			pad.step = 1;
			
			msg.send("现在请输入要隐藏的字符 :").exec(data);
			
		} else {
			
			executeAsync(new SendDocument(msg.chatId(),StrUtil.utf8Bytes(ZeroPad.encodeTo(pad.text,msg.text()))).fileName("Result.TXT").caption("编码完成 :)"));
			
			clearPrivatePoint(user);
			
		}
		
	}
	
}
