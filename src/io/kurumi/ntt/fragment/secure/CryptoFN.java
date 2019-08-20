package io.kurumi.ntt.fragment.secure;

import cn.hutool.crypto.Mode;
import io.kurumi.ntt.db.PointData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;

public class CryptoFN extends Fragment {

	final String POINT_SE = "crypto_se";
	
	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerFunction("aes","aesd","des","desd","rsa","rsad","rc4","rc4d");

		registerFunction(POINT_SE);
		
	}
	
	class SymmetricEncryption extends PointData {
		
		String method;
		
		boolean dec;
		
		Mode mode;
		
		Padding padding;
		
		byte[] iv;
		
	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		if (function.startsWith("aes") || function.startsWith("des")) {
			
			SymmetricEncryption se = new SymmetricEncryption();
			
			se.method = function.substring(0,3);
			
			se.dec = function.endsWith("d");
			
			//setPrivatePoint(
			
			//msg.send("选择加密方式 :").keyboardVertical((Mode.values())).async();
			
		}
		
	}

}
