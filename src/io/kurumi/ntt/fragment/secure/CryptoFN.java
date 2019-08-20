package io.kurumi.ntt.fragment.secure;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.asymmetric.AbstractAsymmetricCrypto;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.crypto.symmetric.DES;
import cn.hutool.crypto.symmetric.DESede;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import io.kurumi.ntt.db.PointData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.Html;
import cn.hutool.crypto.asymmetric.SM2;
import cn.hutool.crypto.symmetric.RC4;
import cn.hutool.core.util.CharsetUtil;

public class CryptoFN extends Fragment {

	final String POINT_SE = "crypto_se";
	final String POINT_AE = "crypto_ae";
	final String POINT_RC4 = "crypto_rc4";

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerFunction("aes","aesd","des","desd","desede","deseded","rsa","rsad","sm2","sm2d","rc4","rc4d");

		registerPoint(POINT_SE,POINT_AE,POINT_RC4);

	}

	class SymmetricEncryption extends PointData {

		String method;

		boolean dec;

		Mode mode;

		Padding padding;

		byte[] password;

		byte[] iv;

		boolean toBase64;

		public SymmetricEncryption(Msg command) { super(command); }

	}

	class AsymmetricEncryption extends PointData {

		boolean dec;

		boolean isPublic;

		String method;

		String key;

		boolean toBase64;

		public AsymmetricEncryption(Msg command) { super(command); }

	}

	class RC4Encryption extends PointData {

		boolean dec;

		String key;

		boolean toBase64;

		public RC4Encryption(Msg command) { super(command); }

	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		msg.sendTyping();

		if (function.startsWith("aes") || function.startsWith("des")) {

			SymmetricEncryption se = new SymmetricEncryption(msg);

			se.dec = function.endsWith("d");

			se.method = se.dec ? function.substring(0,function.length() - 1) : function;

			setPrivatePoint(user,POINT_SE,se);

			msg.send("选择加密方式 :").keyboardVertical((Object[])Mode.values()).withCancel().exec(se);

		} else if (function.startsWith("rsa") || function.startsWith("sm2")) {

			AsymmetricEncryption ae = new AsymmetricEncryption(msg);

			ae.dec = function.endsWith("d");
			ae.method = ae.dec ? function.substring(0,function.length() - 1) : function;

			setPrivatePoint(user,POINT_AE,ae);

			msg.send("请选择使用的秘钥 :").keyboardHorizontal("公钥","私钥").withCancel().exec(ae);

		} else {

			RC4Encryption re = new RC4Encryption(msg);

			re.dec = function.endsWith("d");

			setPrivatePoint(user,POINT_RC4,re);

			msg.send("输入密码 ( 5 - 256 ): ").withCancel().exec(re);

		}

	}

	@Override
	public void onPoint(UserData user,Msg msg,String point,PointData data) {

		if (!msg.hasText()) {

			clearPrivatePoint(user);

			return;

		}

		msg.sendTyping();
		
		data.with(msg);

		if (POINT_SE.equals(point)) {

			SymmetricEncryption se = (SymmetricEncryption) data;

			if (data.type == 0) {

				try {

					se.mode = Mode.valueOf(msg.text());

				} catch (Exception ex) {}

				if (se.mode == null) {

					clearPrivatePoint(user);

					return;

				}

				data.step = 1;

				msg.send("选择对齐方式 :").keyboardVertical((Object[])Padding.values()).withCancel().exec(data);

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

				if (se.dec) {

					data.step = 5;

					msg.send("请输入密文 ( Hex 或 Base 64)").withCancel().exec(data);

				} else {

					data.step = 4;

					msg.send("选择输出格式").keyboardHorizontal("Hex","Base64").withCancel().exec(data);

				}


			} else if (data.step == 4) {

				se.toBase64 = "base64".equals(msg.text());

				data.step = 5;

				msg.send("请输入文本").withCancel().exec(data);

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

						result = se.toBase64 ? sc.encryptBase64(msg.text()) : sc.encryptHex(msg.text());

					} else {

						result = sc.decryptStr(msg.text());

					}

				} catch (Exception ex) {

					msg.send("错误 : {}",ex.getMessage()).async();

					return;

				}

				msg.send("结果 : {}",Html.code(result)).html().async();

			}

		} else if (POINT_AE.equals(point)) {

			AsymmetricEncryption ae = (AsymmetricEncryption) data;

			if (data.type == 0) {

				ae.isPublic = "公钥".equals(msg.text());

				ae.step = 1;

				msg.send("请输入秘钥文本 ( Hex 或 Base64 ) :").withCancel().exec(data);

			} else if (data.type == 1) {

				ae.key = msg.text();

				if (ae.dec) {

					data.step = 3;

					msg.send("请输入密文 ( Hex 或 Base 64)").withCancel().exec(data);

				} else {

					data.step = 2;

					msg.send("选择输出格式").keyboardHorizontal("Hex","Base64").withCancel().exec(data);

				}


			} else if (data.step == 2) {

				ae.toBase64 = "base64".equals(msg.text());

				data.step = 3;

				msg.send("请输入文本").withCancel().exec(data);

			} else {

				clearPrivatePoint(user);

				String result;

				try {

					AbstractAsymmetricCrypto aa;

					if ("rsa".equals(ae.method)) {

						if (ae.isPublic) {

							aa = new RSA(null,ae.key);

						} else {

							aa = new RSA(ae.key,null);

						}

					} else {

						if (ae.isPublic) {

							aa = new SM2(null,ae.key);

						} else {

							aa = new SM2(ae.key,null);

						}


					}

					if (!ae.dec) {

						result = ae.toBase64 ? aa.encryptBase64(msg.text(),ae.isPublic ? KeyType.PublicKey : KeyType.PrivateKey): aa.encryptHex(msg.text(),ae.isPublic ? KeyType.PublicKey : KeyType.PrivateKey);

					} else {

						result = aa.decryptStr(msg.text(),ae.isPublic ? KeyType.PublicKey : KeyType.PrivateKey);

					}

				} catch (Exception ex) {

					msg.send("错误 : {}",ex.getMessage()).async();

					return;

				}

				msg.send("结果 : {}",Html.code(result)).html().async();

			}

		} else if (POINT_RC4.equals(point)) {

			RC4Encryption re = (RC4Encryption) data;

			if (data.step == 0) {

				re.key = msg.text();

				if (re.dec) {

					data.step = 2;

					msg.send("请输入密文 ( Hex 或 Base 64)").withCancel().exec(data);

				} else {

					data.step = 1;

					msg.send("选择输出格式").keyboardHorizontal("Hex","Base64").withCancel().exec(data);

				}

			} else if (data.step == 1) {

				re.toBase64 = "base64".equals(msg.text());

				data.step = 2;

				msg.send("请输入文本").withCancel().exec(data);

			} else {

				clearPrivatePoint(user);

				String result;

				try {

					RC4 rc4 = new RC4(re.key);

					if (!re.dec) {

						result = re.toBase64 ? rc4.encryptBase64(msg.text(),CharsetUtil.CHARSET_UTF_8) : rc4.encryptHex(msg.text(),CharsetUtil.CHARSET_UTF_8);

					} else {

						result = rc4.decrypt(StrUtil.utf8Bytes(msg.text()),CharsetUtil.CHARSET_UTF_8);

					}

				} catch (Exception ex) {

					msg.send("错误 : {}",ex.getMessage()).async();

					return;

				}

				msg.send("结果 : {}",Html.code(result)).html().async();


			}

		}

	}

}
