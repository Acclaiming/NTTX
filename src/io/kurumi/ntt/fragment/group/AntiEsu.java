package io.kurumi.ntt.fragment.group;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONArray;
import io.kurumi.ntt.db.LocalData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.utils.NTT;
import java.security.acl.Group;
import java.util.LinkedList;
import io.kurumi.ntt.fragment.BotFragment;

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

		"hanaakari"

    };

    public static AntiEsu INSTANCE = new AntiEsu();
    public static JSONArray enable = LocalData.getJSONArray("data","anti_esu",true);
    static String[] keys = new String[]{

		"🐴", "🐮", "🍺", "👊", "震撼", "废物", "弱智", "¿", "96子", "恁", "魔怔", "碰瓷", "寻思", "傻逼",

		"出道", "hj", "户籍", "牛(子|啤|逼)", "领证", "野爹", "夜蝶", "这事", "ao的",

		"迫真", "察觉", "无关心", "便乘", "棒读", "谔谔", "辱骂", "好时代",

		"114", "514", "兄贵", "姐贵", "bb", "仙贝", "先辈","壬",

		"草", "恶臭", "池沼", "噔噔咚", "心肺停止", "激寒", "雷普",

		"林檎", "难视", "人间之", "并感", "饼干", "小鬼", "震声",

		"直球", "屑", "鉴", "野兽", "一般通过", "神必", "削除", "寻思",

		"杰哥", "阿杰", "如果早知道", "不要啊", "兄啊", "高雅", "正义",

		"，，+", "野蛮", "文明", "大脑", "最后警告", "黑屁", "确信",

		"创蜜", "谢绝", "创谢", "创拜", "创安", "创不起", "创哀", "创持", "已踢",

		"亲甜滴", "喷香滴", "创死我了", "太创了", "姥姥", "啃", "创象",

		"自嘲完美", "蛆", "完美华丽", "仏", "那您", "奇妙深刻", "唐突", "震撼",

		"操", "实名","闸总","芬芳","完完全全","橄榄","干烂","您"

    };

    static {

        StringBuilder kw = new StringBuilder(".*(");

        for (int index = 0; index < keys.length; index++) {

            if (index == 0) kw.append(keys[0]);
            else kw.append("|").append(keys[index]);

        }

        kw.append(").*");

        regex = kw.toString();

    }

    public static void save() {

        LocalData.setJSONArray("data","anti_esu",enable);

    }

    public static boolean keywordMatch(String msg) {

		if (msg == null) return false;

        return msg.matches(regex);

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

			if (msg.hasText() && msg.text().replace(" ","").matches(regex)) {

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
