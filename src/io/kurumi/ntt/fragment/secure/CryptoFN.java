package io.kurumi.ntt.fragment.secure;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.crypto.symmetric.DES;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import io.kurumi.ntt.db.PointData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.Html;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.DESede;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.asymmetric.KeyType;

public class CryptoFN extends Fragment {

	final String POINT_SE = "crypto_se";
	final String POINT_RSA = "crypto_rsa";
	
	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerFunction("aes","aesd","des","desd","desede","deseded","rsa","rsad","rc4","rc4d");

		registerFunction(POINT_SE);

	}

	class SymmetricEncryption extends PointData {

		String method;

		boolean dec;

		Mode mode;

		Padding padding;

		byte[] password;

		byte[] iv;

		public SymmetricEncryption(Msg command) { super(command); }

	}
	
	class RsaEncryption extends PointData {
	
		boolean dec;

		boolean isPublic;
		
		String key;
		
		public RsaEncryption(Msg command) { super(command); }

	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		msg.sendTyping();
		
		if (function.startsWith("aes") || function.startsWith("des")) {

			SymmetricEncryption se = new SymmetricEncryption(msg);

			se.dec = function.endsWith("d");
			
			se.method = se.dec ? function.substring(0,function.length() - 1) : function;

			setPrivatePoint(user,POINT_SE,se);

			msg.send("选择加密方式 :").keyboardVertical(Mode.values()).withCancel().exec(se);

		} else if (function.startsWith("rsa")) {
		
			RsaEncryption rsa = new RsaEncryption(msg);
			
			rsa.dec = function.endsWith("d");
			
			setPrivatePoint(user,POINT_RSA,rsa);
			
			msg.send("请输入使用的秘钥 :").keyboardHorizontal("公钥","私钥").withCancel().exec(rsa);
			
		}

	}

	@Override
	public void onPoint(UserData user,Msg msg,String point,PointData data) {

		if (!msg.hasText()) {

			clearPrivatePoint(user);

			return;

		}

		msg.sendTyping();
		
		if (POINT_SE.equals(point)) {

			SymmetricEncryption se = (SymmetricEncryption) data.with(msg);

			if (data.type == 0) {

				try {

					se.mode = Mode.valueOf(msg.text());

				} catch (Exception ex) {}

				if (se.mode == null) {

					clearPrivatePoint(user);

					return;

				}

				data.step = 1;

				msg.send("选择对齐方式 :").keyboardVertical(Padding.values()).withCancel().exec(data);

			} else if (data.step == 1) {

				try {

					se.padding = Padding.valueOf(msg.text());

				} catch (Exception ex) {}

				if (se.padding == null) {

					clearPrivatePoint(user);

					return;

				}

				data.step = 2;

				msg.send("请输入密码 :").withCancel().exec(data);

			} else if (data.step == 3) {

				se.password = StrUtil.utf8Bytes(msg.text());

				data.step = 4;

				msg.send("请输入" + (se.dec ? "密文" : "文本")).withCancel().exec(data);

			} else {

				clearPrivatePoint(user);
				
				String result;

				try {

					SymmetricCrypto sc;

					if ("aes".equals(se.method)) {

						sc = new AES(se.mode,se.padding,se.password);

					} else if ("desede".equals(se.method)) {

						sc = new DESede(se.mode,se.padding,se.password);
					
						} else {
						
						sc = new DES(se.mode,se.padding,se.password);

					}
					
					if (!se.dec) {

						result = sc.encryptHex(msg.text());

					} else {

						result = sc.decryptStr(msg.text());

					}

				} catch (Exception ex) {

					msg.send("错误 : {}",ex.getMessage()).async();

					return;

				}
			
				msg.send("结果 : {}",Html.code(result)).async();

			}

		} else if (POINT_RSA.equals(point)) {
			
			RsaEncryption rsa = (RsaEncryption) data;
			
			if (data.type == 0) {
				
				rsa.isPublic = "公钥".equals(msg.text());
				
				rsa.step = 1;
				
				msg.send("请输入秘钥文本 ( Hex 或 Base64 ) :").withCancel().exec(data);
				
			} else if (data.type == 1) {
				
				rsa.key = msg.text();

				rsa.step = 2;

				msg.send("请输入" + (rsa.dec ? "密文" : "文本")).withCancel().exec(data);
				
			} else {
				
				clearPrivatePoint(user);

				String result;

				try {

					RSA re;
					
					if (rsa.isPublic) {
						
						re = new RSA(null,rsa.key);

					} else {
						
						re = new RSA(rsa.key,null);
						
					}
						
					if (!rsa.dec) {

						result = re.encryptHex(msg.text(),rsa.isPublic ? KeyType.PublicKey : KeyType.PrivateKey);

					} else {

						result = re.decryptStr(msg.text(),rsa.isPublic ? KeyType.PublicKey : KeyType.PrivateKey);

					}

				} catch (Exception ex) {

					msg.send("错误 : {}",ex.getMessage()).async();

					return;

				}

				msg.send("结果 : {}",Html.code(result)).async();

				
				
			}
			
		}

	}

}
