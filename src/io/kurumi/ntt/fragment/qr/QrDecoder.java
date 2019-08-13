package io.kurumi.ntt.fragment.qr;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;
import com.google.zxing.MultiFormatReader;
import cn.hutool.core.util.ImageUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.core.util.StrUtil;
import io.kurumi.ntt.utils.Html;

public class QrDecoder extends Fragment {

	@Override
	public void init(BotFragment origin) {
		
		super.init(origin);
		
		registerFunction("qr_decode");
		
	}

	@Override
	public int checkFunctionContext(UserData user,Msg msg,String function,String[] params) {
		
		return FUNCTION_PUBLIC;
		
	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
		if (!msg.isReply() || msg.replyTo().message().photo() == null) {
			
			msg.send("请对图片回复 :)").async();
			
			return;
			
		}
		
		String result = QrCodeUtil.decode(msg.replyTo().photo());

		if (StrUtil.isBlank(result)) {
			
			msg.send("无结果 :)").async();
			
		} else {
			
			msg.send("结果 :\n",Html.code(result)).html().async();
			
		}
		
	}
	
}
