package io.kurumi.ntt.fragment.group;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONArray;
import io.kurumi.ntt.db.LocalData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.utils.Html;
import io.kurumi.ntt.utils.NTT;
import java.util.HashSet;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class AntiEsu extends Fragment {

    public static final String regex;
	public static final String base;
    public static final String[] stickers = new String[]{

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

    public static JSONArray enable = LocalData.getJSONArray("data","anti_esu",true);

	static String[] pinyinKeys = new String[] {

		"恶俗","迫害","要素","我哭哭",

		"震撼", "废物", "弱智", "魔怔","碰瓷", "寻思", "傻逼",
		"迫真", "察觉", "无关心", "便乘", "棒读","你妈","野爹",
		"兄贵", "姐贵", "仙贝", "先辈", "好时代",

		"池沼", "噔噔咚", "心肺停止", "激寒", "雷普",

		"林檎", "难视", "人间之", "并感", "小鬼", "震声","硬汉",
		"直球", "野兽", "一般通过", "神必", "削除", "寻思",
		"出道","户籍","高雅", "正义","恶臭",
		
		"野蛮", "大脑", "最后警告", "黑屁", "确信",

		"创蜜", "谢绝", "创谢", "创拜", "创安", "创不起", "创哀", "创持", "已踢",

		"亲甜滴", "喷香滴", "创死我了", "太创了", "姥姥", "创象",

		"自嘲完美",  "完美华丽", "奇妙深刻", "唐突", "震撼我","实名上网",

		"闸总","芬芳","完完全全","干烂","小将",

	};


    static String[] keys = new String[]{

		"🐴", "🐮", "🍺", "👊", "¿", "恁","蛆",

		"nmsl", "这事", "ao的","niu(pi|bi)","[^不]屑", "鉴", 

		"谔谔", "呃呃","蛆","草","神触","怼",

		"114514", "壬","我局","局(的|得)","事你",

		"杰哥", "阿杰", "如果早知道", "不要啊", "兄啊", "，，，+", 

		"esu\\.(wiki|moe|zone)","zhina\\.(wiki|red)"

    };

	static HanyuPinyinOutputFormat format;

    static {

		format = new HanyuPinyinOutputFormat();

		format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		format.setToneType(HanyuPinyinToneType.WITH_TONE_NUMBER);
		format.setVCharType(HanyuPinyinVCharType.WITH_V);

		StringBuilder bw = new StringBuilder(".*(");
		
		for (int index = 0; index < keys.length; index++) {

			if (index > 0) bw.append("|");
			
			bw.append(keys[index]);

        }
		
		for (int index = 0; index < pinyinKeys.length; index++) {
			
			bw.append("|").append(pinyinKeys[index]);

        }
		

        bw.append(").*");

        base = bw.toString();
		
		
        StringBuilder kw = new StringBuilder(".*(");

        for (int index = 0; index < pinyinKeys.length; index++) {

			StringBuilder kk = new StringBuilder();

			char[] key = pinyinKeys[index].toCharArray();

			for (char c : key) {


				try {

					String[] pinyin = PinyinHelper.toHanyuPinyinStringArray(c,format);

					if (pinyin == null) {

						kk.append(c);

					} else {

						HashSet<String> set = new HashSet<String>();

						for (String p : pinyin) {

							set.add(p);

						}

						pinyin = set.toArray(new String[set.size()]);

						if (pinyin.length == 1) {

							kk.append(pinyin[0]);

						} else {

							kk.append("(").append(ArrayUtil.join(pinyin,"|")).append(")");

						}

					}


				} catch (BadHanyuPinyinOutputFormatCombination ex) {

					kk.append(c);

				}

			}

            if (index == 0) {

				kw.append(kk.toString());

			} else {

				kw.append("|").append(kk.toString());

			}

        }

		for (int index = 0; index < keys.length; index++) {

			kw.append("|").append(keys[index]);

        }

        kw.append(").*");

        regex = kw.toString();

    }

    public static void save() {

        LocalData.setJSONArray("data","anti_esu",enable);

    }

	public String toPinyin(String msg) {

		StringBuilder text = new StringBuilder();

		for (char c : msg.replace(" ","").toLowerCase().toCharArray()) {

			try {

				String[] pinyin = PinyinHelper.toHanyuPinyinStringArray(c,format);

				if (pinyin == null || pinyin.length == 0) text.append(c);
				else text.append(pinyin[0]);

			} catch (BadHanyuPinyinOutputFormatCombination e) {

				text.append(c);

			}

		}

		return text.toString();

	}

    public static boolean keywordMatch(String msg) {

		if (msg == null) return false;

		StringBuilder text = new StringBuilder();

		for (char c : msg.replace(" ","").toLowerCase().toCharArray()) {

			try {

				String[] pinyin = PinyinHelper.toHanyuPinyinStringArray(c,format);

				if (pinyin == null || pinyin.length == 0) text.append(c);
				else text.append(pinyin[0]);

			} catch (BadHanyuPinyinOutputFormatCombination e) {

				text.append(c);

			}

		}

		return text.toString().matches(regex) || msg.matches(regex);

	}

	@Override
	public int checkFunction() {

		return FUNCTION_GROUP;

	}

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

        registerFunction("antiesu","py");

	}

    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params) {

		if ("py".equals(function)) {
		
			msg.send(Html.code(toPinyin(ArrayUtil.join(ArrayUtil.remove(params,0)," ")))).html().exec();
			
			return;
			
		}
		
        if (NTT.checkGroupAdmin(msg)) return;

        if (params.length == 1 && "off".equals(params[0])) {

            if (!enable.contains(msg.chatId().longValue())) {

                msg.send("无需重复关闭 ~").exec();

            } else {

                enable.remove(msg.chatId().longValue());

                save();

                msg.send("关闭成功 ~").exec();

            }

        } else {

            if (enable.contains(msg.chatId().longValue())) {

                msg.send("没有关闭 ~").exec();

            } else {

                enable.add(msg.chatId().longValue());

                save();

                msg.send("已开启 ~").exec();

            }

        }

    }

	@Override
	public int checkMsg(UserData user,Msg msg) {

		if (msg.isGroup() && enable.contains(msg.chatId().longValue())) {

			if (msg.hasText() && keywordMatch(msg.text())) {

				msg.delete();

				return PROCESS_REJECT;

			} else if (msg.message().sticker() != null) {

				if (ArrayUtil.contains(stickers,msg.message().sticker().setName())) {

					msg.delete();

					return PROCESS_REJECT;

				}

			}

		}

		return PROCESS_ASYNC;

	}

}
