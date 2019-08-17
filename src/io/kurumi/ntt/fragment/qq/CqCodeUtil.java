package io.kurumi.ntt.fragment.qq;

import java.util.HashMap;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.NumberUtil;

public class CqCodeUtil {
	
	public static HashMap<Integer,String> emojiMap;
	
	public static String replaceFace(String message) {
		
		String cqFace = "[CQ:face,id=";
		
		while (message.contains(cqFace)) {
			
			String left = StrUtil.subBefore(message,cqFace,false);
			
			message = StrUtil.subAfter(message,cqFace,false);
			
			String face = StrUtil.subBefore(message,"]",false);
			
			face = emojiMap.get(NumberUtil.parseInt(face));
			
			message = left + face + StrUtil.subAfter(message,"]",false);
			
		}
		
		return message;
		
	}
	
	static {
			
		emojiMap.put(0,"\u0001\uF62E");
		emojiMap.put(1,"\u0001\uF623");
		emojiMap.put(2,"\u0001\uF60D");
		emojiMap.put(3,"\u0001\uF633");
		emojiMap.put(4,"\u0001\uF60E");
		emojiMap.put(5,"\u0001\uF62D");
		emojiMap.put(6,"\u0000\u263A");
		emojiMap.put(7,"\u0001\uF637");
		emojiMap.put(8,"\u0001\uF634");
		emojiMap.put(9,"\u0001\uF62D");
		emojiMap.put(10,"\u0001\uF630");
		emojiMap.put(11,"\u0001\uF621");
		emojiMap.put(12,"\u0001\uF61D");
		emojiMap.put(13,"\u0001\uF603");
		emojiMap.put(14,"\u0001\uF642");
		emojiMap.put(15,"\u0001\uF641");
		emojiMap.put(16,"\u0001\uF913");
		emojiMap.put(18,"\u0001\uF624");
		emojiMap.put(19,"\u0001\uF628");
		emojiMap.put(20,"\u0001\uF60F");
		emojiMap.put(21,"\u0001\uF60A");
		emojiMap.put(22,"\u0001\uF644");
		emojiMap.put(23,"\u0001\uF615");
		emojiMap.put(24,"\u0001\uF924");
		emojiMap.put(25,"\u0001\uF62A");
		emojiMap.put(26,"\u0001\uF628");
		emojiMap.put(27,"\u0001\uF613");
		emojiMap.put(28,"\u0001\uF62C");
		emojiMap.put(29,"\u0001\uF911");
		emojiMap.put(30,"\u0001\uF44A");
		emojiMap.put(31,"\u0001\uF624");
		emojiMap.put(32,"\u0001\uF914");
		emojiMap.put(33,"\u0001\uF910");
		emojiMap.put(34,"\u0001\uF635");
		emojiMap.put(35,"\u0001\uF629");
		emojiMap.put(36,"\u0001\uF47F");
		emojiMap.put(37,"\u0001\uF480");
		emojiMap.put(38,"\u0001\uF915");
		emojiMap.put(39,"\u0001\uF44B");
		emojiMap.put(50,"\u0001\uF641");
		emojiMap.put(51,"\u0001\uF913");
		emojiMap.put(53,"\u0001\uF624");
		emojiMap.put(54,"\u0001\uF92E");
		emojiMap.put(55,"\u0001\uF628");
		emojiMap.put(56,"\u0001\uF613");
		emojiMap.put(57,"\u0001\uF62C");
		emojiMap.put(58,"\u0001\uF911");
		emojiMap.put(73,"\u0001\uF60F");
		emojiMap.put(74,"\u0001\uF60A");
		emojiMap.put(75,"\u0001\uF644");
		emojiMap.put(76,"\u0001\uF615");
		emojiMap.put(77,"\u0001\uF924");
		emojiMap.put(78,"\u0001\uF62A");
		emojiMap.put(79,"\u0001\uF44A");
		emojiMap.put(80,"\u0001\uF624");
		emojiMap.put(81,"\u0001\uF914");
		emojiMap.put(82,"\u0001\uF910");
		emojiMap.put(83,"\u0001\uF635");
		emojiMap.put(84,"\u0001\uF629");
		emojiMap.put(85,"\u0001\uF47F");
		emojiMap.put(86,"\u0001\uF480");
		emojiMap.put(87,"\u0001\uF915");
		emojiMap.put(88,"\u0001\uF44B");
		emojiMap.put(96,"\u0001\uF630");
		emojiMap.put(97,"\u0001\uF605");
		emojiMap.put(98,"\u0001\uF925");
		emojiMap.put(99,"\u0001\uF44F");
		emojiMap.put(100,"\u0001\uF922");
		emojiMap.put(101,"\u0001\uF62C");
		emojiMap.put(102,"\u0001\uF610");
		emojiMap.put(103,"\u0001\uF610");
		emojiMap.put(104,"\u0001\uF629");
		emojiMap.put(105,"\u0001\uF620");
		emojiMap.put(106,"\u0001\uF61E");
		emojiMap.put(107,"\u0001\uF61F");
		emojiMap.put(108,"\u0001\uF60F");
		emojiMap.put(109,"\u0001\uF619");
		emojiMap.put(110,"\u0001\uF627");
		emojiMap.put(111,"\u0001\uF920");
		emojiMap.put(172,"\u0001\uF61C");
		emojiMap.put(173,"\u0001\uF62D");
		emojiMap.put(174,"\u0001\uF636");
		emojiMap.put(175,"\u0001\uF609");
		emojiMap.put(176,"\u0001\uF913");
		emojiMap.put(177,"\u0001\uF635");
		emojiMap.put(178,"\u0001\uF61C");
		emojiMap.put(179,"\u0001\uF4A9");
		emojiMap.put(180,"\u0001\uF633");
		emojiMap.put(181,"\u0001\uF913");
		emojiMap.put(182,"\u0001\uF602");
		emojiMap.put(183,"\u0001\uF913");
		emojiMap.put(212,"\u0001\uF633");
		
	}
	
}
