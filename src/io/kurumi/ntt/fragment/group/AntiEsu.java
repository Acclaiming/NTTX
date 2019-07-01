package io.kurumi.ntt.fragment.group;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONArray;
import io.kurumi.ntt.db.LocalData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.utils.BotLog;
import io.kurumi.ntt.utils.NTT;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class AntiEsu extends Fragment {

    static final String regex;
    static final String[] stickers = new String[]{

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

    public static AntiEsu INSTANCE = new AntiEsu();
    public static JSONArray enable = LocalData.getJSONArray("data","anti_esu",true);

	static String[] pinyinKeys = new String[] {

		"恶俗",
		
		"震撼", "废物", "弱智", "魔怔", "碰瓷", "寻思", "傻逼",
		"迫真", "察觉", "无关心", "便乘", "棒读","你妈","野爹",
		"兄贵", "姐贵", "仙贝", "先辈","草","辱骂", "好时代",

		"池沼", "噔噔咚", "心肺停止", "激寒", "雷普",

		"林檎", "难视", "人间之", "并感", "小鬼", "震声","硬汉",
		"直球", "屑", "鉴", "野兽", "一般通过", "神必", "削除", "寻思",
		"出道","户籍","高雅", "正义","恶臭",

		"野蛮", "文明", "大脑", "最后警告", "黑屁", "确信",

		"创蜜", "谢绝", "创谢", "创拜", "创安", "创不起", "创哀", "创持", "已踢",

		"亲甜滴", "喷香滴", "创死我了", "太创了", "姥姥", "创象",

		"自嘲完美", "蛆", "完美华丽", "奇妙深刻", "唐突", "震撼","实名",

		"闸总","芬芳","完完全全","干烂","恶俗",

	};


    static String[] keys = new String[]{

		"🐴", "🐮", "🍺", "👊", "¿", "恁","蛆","fo了",

		"nm$l","nmsl", "hj", "牛(子|啤|逼)", "这事", "ao的",

		"谔谔", "呃呃","您",

		"114", "514", "壬","我局","局(的|得)","事你",

		"杰哥", "阿杰", "如果早知道", "不要啊", "兄啊", "，，+", 
		"操", "您",
		"esu\\.(wiki|moe|zone)","zhina\\.(wiki|red)"

    };

	static HanyuPinyinOutputFormat format;

    static {

		format = new HanyuPinyinOutputFormat();

		format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		format.setVCharType(HanyuPinyinVCharType.WITH_V);

        StringBuilder kw = new StringBuilder(".*(");

        for (int index = 0; index < pinyinKeys.length; index++) {

			StringBuilder kk = new StringBuilder();

			char[] key = pinyinKeys[index].toCharArray();

			for (char c : key) {


				try {

					String[] pinyin = PinyinHelper.toHanyuPinyinStringArray(c,format);

					if (pinyin == null) kk.append(c);
					
					else {
						
						if (pinyin.length == 1) kk.append(pinyin[0]);
						
						else {
						
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
		
		BotLog.debug(regex);

    }

    public static void save() {

        LocalData.setJSONArray("data","anti_esu",enable);

    }

    public static boolean keywordMatch(String msg) {

		if (msg == null) return false;
		
		try {
			
			return PinyinHelper.toHanYuPinyinString(msg,format,"",true).matches(regex);
			
		} catch (BadHanyuPinyinOutputFormatCombination e) {
			
			return false;
			
		}
		
    }

	@Override
	public int checkFunction() {

		return FUNCTION_GROUP;

	}

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

        registerFunction("antiesu");

	}

    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params) {

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

			if (msg.hasText() && msg.text().replaceAll(" ","").matches(regex)) {

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
