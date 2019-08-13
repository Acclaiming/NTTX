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

public class QrEncoder extends Fragment {

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerFunction("qr_encode");

	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		if (params.length == 0) {

			msg.invalidParams("文本...").async();

			return;

		}

		File cacheFile = new File(Env.CACHE_DIR,"qr_gen/" + UUID.fastUUID().toString(true) + ".jpg");

		cacheFile.getParentFile().mkdirs();

		QrCodeUtil.generate(ArrayUtil.join(params," "),500,500,cacheFile);

		msg.sendUpdatingPhoto();

		execute(new SendPhoto(msg.chatId(),cacheFile));
		
		cacheFile.delete();

	}

}
