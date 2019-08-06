package io.kurumi.ntt.fragment.netease;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import java.io.File;
import com.pengrad.telegrambot.request.SendAudio;

public class NeteaseMusic extends Fragment {

	@Override
	public void init(BotFragment origin) {
		
		super.init(origin);
		
		registerFunction("music");
		
	}

	/*
	
	static AES encoder = new AES(Mode.ECB,Padding.NoPadding,"e82ckenh8dichen8".getBytes());

	static String mkParams(String path,String dict) {

		String sign = SecureUtil.md5("nobody" + path + "use" + dict + "md5forencrypt").toLowerCase();

		String text = path + "-36cd479b6b5-" + dict + "-36cd479b6b5-" + sign;

		int fill = 16 - (text.length() % 16);

		for (int index = 0;index < fill;index ++) {

			text += (char)0x0d;

		}

		return encoder.encryptHex(text).toUpperCase();


	}
	
	*/
	
	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		String text = ArrayUtil.join(params," ");
	
		String name = null;

		if (text.contains("《")) {

			name = StrUtil.subBetween(text,"《","》");

		}
		
		if (text.contains("song/")) text = StrUtil.subBetween(text,"song/","/");

		if (!NumberUtil.isNumber(text)) {
			
			msg.send("请输入正确的链接、音乐ID！").async();
			
			return;
			
		}
		
		String link = getDownloadLink(text,Env.NETEASE_COOKIE);
	
		String suffix = StrUtil.subAfter(link,".",true);
		
		File cache = new File(Env.CACHE_DIR,"music/" + text + "." + suffix);
		
		if (name == null) name = text;
		
		name += "." + suffix;
		
		if (!cache.isFile()) {
			
			HttpUtil.downloadFile(link,cache);
			
		}
		
		msg.sendUpdatingAudio();
		
		executeAsync(new SendAudio(msg.chatId(),cache).fileName(name));
		
	}
	

	public static String getDownloadLink(String musicId,String cookie) {

		// String path = "/api/song/enhance/download/url";

		// 320000 极高 999000 无损
		
		String url = "https://interface3.music.163.com/api/song/enhance/download/url?br=999000&id=" + musicId + "_0";

		String userAgent = "NeteaseMusic/6.3.0.1563892465(149);Dalvik/2.1.0 (Linux; U; Android 9; TEST Build/114514)";

		HttpRequest request = HttpUtil.createGet(url);

		request.header(Header.USER_AGENT,userAgent);
		
		request.cookie(cookie);

		HttpResponse result = request.execute();
	
		return new JSONObject(result.body()).getByPath("data.url",String.class);

		/*
		
		// request.contentType("application/x-www-form-urlencoded");

		// String deviceId = randomTelephonyGetDeviceId() +  "\t02:00:00:00:00:00\t5106025eb79a5247\t70ffbaac7";

	    // deviceId = Base64.encode(deviceId);

		// request.cookie("__csrf=" + __csrf + ";MUSIC_U=" + MUSIC_U + ";");

		// request.cookie("osver=9; MUSIC_U=ddd5d2b14cbc80bfd38f91958593841cad2f66cc62903eba5e489a1477a680f1f8e06c761a0f9e2ee44759ffd0c57c8efe2897047e8106fb; versioncode=151; buildver=1565092410; resolution=2047x1080; __remember_me=true; os=android; channel=netease; deviceId=ODY3NjAxMDMzOTA3MDM4CTAyOjAwOjAwOjAwOjAwOjAwCTYxNGU2NTQ4ZWQzM2I0ZjcJYzUxMWIyNjY%3D; appver=6.3.1; mobilename=MIX2S; __csrf=6ff80d978d0d4dd18fd852837352e1f4; ntes_kaola_ad=1");

		// request.cookie("buildver=1563968610; resolution=2029x1080; remember_me=true; csrf=142aa4fb754acbee025acc1d2db6d5ae; osver=9; deviceId=ODY5Nzg1MDMzNDUwOTEzCTAyOjAwOjAwOjAwOjAwOjAwCTI5NzVkOWNmMjE0OWMyZGMJODVmN2Y2ODg%3D; appver=6.3.0; MUSIC_U=1cccf612699d92f7335687ea17c302461ee2af552173424b0c19643c29067f7e5f2a08a8dcf2f506f154d2022a1542a9384fe0dd1eca3a5f; versioncode=149; ntes_kaola_ad=1; mobilename=MI8UD; os=android; channel=netease");

		//request.header("X-Real-IP","211.161.244.70");
		
		JSONObject params =  new JSONObject();

		params.put("br","999000");
		params.put("e_r","true");
		params.put("id",musicId + "_0");

		JSONObject header = new JSONObject();

		header.put("MUSIC_U",MUSIC_U);
		header.put("__csrf",__csrf);
		
		 header.put("appver","6.3.0");
		 header.put("versioncode","149");
		 header.put("buildver","1563892465");
		 header.put("channel","netease");

		 header.put("deviceId",deviceId);
		 header.put("mobilename","LenovoK30-T");
		 header.put("resolution","1920x1080");
		 header.put("os","android");
		 header.put("osver","9");

		// header.put("requestId",System.currentTimeMillis() + "_3389");

		// params.put("header",header);

		String dict = params.toString();

		//System.out.println(( mkParams(path,dict ));

		//System.out.println(dict);

		//request.form("params","");

		// request.form("params",mkParams(path,dict));

		//request.form("params",mkParams(path,"{\"br\":\"999000\",\"e_r\":\"true\",\"header\":{\"MUSIC_U\":\"87e4f51cf4971aefac8a7d2f82de9e789fe9e78894ae231f990b22389e7315006ca760fc48e91c11de9cae83bc53244dfe2897047e8106fb\",\"__csrf\":\"943f135f4e6ba853b3b580f8246c75ba\",\"appver\":\"6.3.0\",\"buildver\":\"1563892465\",\"channel\":\"netease\",\"deviceId\":\"MzU3NjgxMDgxMjc2NjY1CTAyOjAwOjAwOjAwOjAwOjAwCWYxNjFkZmJhNjg5MTY3MzEJSEFBWkNZMDRaODIyTlNK\",\"mobilename\":\"LenovoK30-T\",\"os\":\"android\",\"osver\":\"4.4\",\"requestId\":\"1565075900973_1351\",\"resolution\":\"1920x1080\",\"versioncode\":\"149\"},\"id\":\"34003790_0\"}"));

		HttpResponse result = request.execute();

		// System.out.println( encoder.decryptStr(result.bodyBytes()));

		System.out.println(result);
		
		*/

	}

	/*
	
	private static Random random = new Random();

	public static String randomTelephonyGetDeviceId() {

        String imeiCode = "86" + randomString(12,false,false,true);

        return imeiCode + IMEIGen.genCode(imeiCode);

    }

	public static String randomString(int length,boolean lowEnglish,boolean upperEnglish,boolean number) {

        String baseString = "";

        if (lowEnglish) baseString += "abcdefghijklmnopqrstuvwxyz";

		if (upperEnglish) baseString += "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

		if (number) baseString += "0123456789";

        StringBuilder sb = new StringBuilder();

		for (int i = 0; i < length; i++) {

            sb.append(baseString.charAt(random.nextInt(baseString.length())));

        }

        return sb.toString();

    }

	public static class IMEIGen {


		static List<String> beachIMEI(String begin,String end) {
			List<String> imeis = new ArrayList<String>();
			try {
				long   count       = Long.parseLong(end) - Long.parseLong(begin);
				Long   currentCode = Long.parseLong(begin);
				String code;
				for (int i = 0; i <= count; i++) {
					code = currentCode.toString();
					code = code + genCode(code);
					imeis.add(code);
					System.out.println("code=====" + code);
					currentCode += 1;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return imeis;
		}

		public static String genCode(String code) {
			int    total = 0, sum1 = 0, sum2 = 0;
			int    temp  = 0;
			char[] chs   = code.toCharArray();
			for (int i = 0; i < chs.length; i++) {
				int num = chs[i] - '0';    // ascii to num
				//System.out.println(num);
				//(1)将奇数位数字相加(从1开始计数)
				if (i % 2 == 0) {
					sum1 = sum1 + num;
				} else {
					//(2)将偶数位数字分别乘以2,分别计算个位数和十位数之和(从1开始计数)
					temp = num * 2;
					if (temp < 10) {
						sum2 = sum2 + temp;
					} else {
						sum2 = sum2 + temp + 1 - 10;
					}
				}
			}
			total = sum1 + sum2;
			//如果得出的数个位是0则校验位为0,否则为10减去个位数
			if (total % 10 == 0) {
				return "0";
			} else {
				return (10 - (total % 10)) + "";
			}

		}

	}
	
	*/
	
}
