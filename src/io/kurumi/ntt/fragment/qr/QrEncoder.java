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
import cn.hutool.core.util.ZipUtil;

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

		int background = 0xE91E63;
		
		int color = 0xffffff;

		if (params.length == 0) {

			msg.send("/qr_encode <文本...>","/qr_encode [背景] <文本...>","/qr_encode [背景] [颜色] <文本...>").async();

			return;

		}
		
		if (params.length > 1) {

			if (params[0].charAt(0) == '#') {

				try {

					background = NumberUtil.parseInt("0x" + params[0].substring(1));

				} catch (Exception ex) {

					msg.send("无效的背景颜色格式 例子 : #E91E63 (Material Pink 500)").async();

					return;

				}

			}

		}
		
		if (params.length > 2) {

			if (params[1].charAt(0) == '#') {

				try {

					color = NumberUtil.parseInt("0x" + params[1].substring(1));

				} catch (Exception ex) {

					msg.send("无效的颜色格式 例子 : #FFFFFF (白色)").async();

					return;

				}

			}

		}
		
		msg.sendUpdatingPhoto();

		File cacheFile = new File(Env.CACHE_DIR,"qr_gen/" + UUID.fastUUID().toString(true) + ".jpg");

		cacheFile.getParentFile().mkdirs();

		ZipUtil.gzip(
		
		QrCodeUtil.generate(ArrayUtil.join(params," "),new QrConfig(512,512).setBackColor(background).setForeColor(color),cacheFile));

		execute(new SendPhoto(msg.chatId(),cacheFile));

		cacheFile.delete();

	}

}
