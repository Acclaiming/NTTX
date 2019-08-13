package io.kurumi.ntt.fragment.qr;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import java.io.File;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.SendResponse;
import cn.hutool.core.util.NumberUtil;

public class QrEncoder extends Fragment {

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerFunction("qr_encode");

	}

	@Override
	public int checkFunctionContext(UserData user,Msg msg,String function,String[] params) {

		return FUNCTION_PUBLIC;

	}
	
	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		int color = 0xE91E63;
		
		if (params.length == 0) {

			msg.send("/qr_encode [#颜色 可选] <文本...>").async();

			return;

		} else if (params.length > 2) {
			
			if (params[0].charAt(0) == '#') {
				
				try {
				
				color = NumberUtil.parseInt("0x" + params[0].substring(1));
				
				} catch (Exception ex) {
					
					msg.send("无效的颜色格式 例子 : #E91E63 (Material Pink 500)").async();
					
					return;
					
				}
				
			}
			
		}

		File cacheFile = new File(Env.CACHE_DIR,"qr_gen/" + UUID.fastUUID().toString(true) + ".jpg");

		cacheFile.getParentFile().mkdirs();

		QrCodeUtil.generate(ArrayUtil.join(params," "),new QrConfig(500,500).setBackColor(color).setForeColor(0xffffff),cacheFile);

		msg.sendUpdatingPhoto();

		execute(new SendPhoto(msg.chatId(),cacheFile));
		
		cacheFile.delete();

	}

}
