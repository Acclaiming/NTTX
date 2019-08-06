package io.kurumi.ntt.fragment.group;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.lang.Matcher;
import java.util.regex.Pattern;

public class MaliciousMessage {

	public static final String[] esuStickers = new String[]{
	 
		"LzhStickers",
		"ESUISGOD",
		"qiezi",
		"Jason_Funderburker",
		"Suicas",
		"jvbao",
		"JieGeBuYao",
		"EsuXi",
		"youdontcry",
		"pandahead",
		"frogthepepe",
		"tieba_new",
		"jiaomoren",
		"weitu",
		"chinapig",
		"duren13",
		"Ruhuaism",
		"EliotStickerPack",
		"myphzy",
		"sikesocute",
		"thonkang",
		"route_boy",
		"MyISP",
		"JojosBA",
		"undertalepack",
		"Chainizi",
		"zhenxiang",
		"dongtu",
		"Tetsaiphoto",
		"YouCountry",
		"piyixia",
		"QQciya",
		"QQciya2",
		"weitu",
		"CyanoxygenS",
		"esugirl",
		"setsunann",
		"OriESG",
		"EsuWiki",
		"hanaakari",
		"idiotmessages",

    };
	
	
    public final static String[] esuKeywords = new String[]{

            "恶俗", "要素", "李威",

            "震撼", "废物", "弱智", "魔怔", "碰瓷", "寻思", "傻逼",
            "迫真", "察觉", "无关心", "便乘", "棒读", "你妈", "野爹",
            "兄贵", "姐贵", "仙贝", "先辈", "好时代",

            "池沼", "噔噔咚", "心肺停止", "激寒", "雷普", "谢谢茄子", "3q2x", "qqqxx",

            "林檎", "难视", "人间之", "并感", "小鬼", "震声", "硬汉",
            "直球", "野兽", "一般通过", "神必", "削除", "寻思",
            "出道", "户籍", "高雅", "正义", "恶臭",

            "野蛮", "大脑", "最后警告", "黑屁", "确信",

            "创蜜", "谢绝", "创哀", "创持", "已踢",

            "亲甜滴", "喷香滴", "创死我了", "太创了", "姥姥", "创象",

            "自嘲完美", "完美华丽", "奇妙深刻", "唐突", "震撼我", "实名上网",

            "闸总", "芬芳", "完完全全", "干烂", "小将",

            "🐴", "🐮", "🍺", "👊", "¿", "恁", "蛆", "我哭哭",

            "这事", "ao的", "牛啤", "[^不]屑", "鉴",

            "谔谔", "呃呃", "蛆", "草", "神触", "怼",

            "114514", "壬", "我局", "局(的|得)", "事你",

            "杰哥", "阿杰", "如果早知道", "不要啊", "兄啊", "，，，+",

            "esu\\.(wiki|moe|zone)", "zhina\\.(wiki|red)"

    };
	
	public static Pattern esuWordsRegex;
	
	static {
		
		StringBuilder kw = new StringBuilder("(");

		for (int index = 0; index < esuKeywords.length; index++) {

			if (index > 0) kw.append("|");

			kw.append(esuKeywords[index]);

        }


        kw.append(")");
		
        esuWordsRegex = Pattern.compile(kw.toString());
		
	}

}
