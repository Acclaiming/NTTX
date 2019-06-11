package io.kurumi.ntt.fragment.group;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONArray;
import io.kurumi.ntt.db.LocalData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.abs.Function;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.utils.NTT;
import java.security.acl.Group;
import java.util.LinkedList;

public class AntiEsu extends Function {

	public static AntiEsu INSTANCE = new AntiEsu();

    public static JSONArray enable = LocalData.getJSONArray("data","anti_esu",true);

    public static void save() {
		
        LocalData.setJSONArray("data","anti_esu",enable);

    }

	@Override
	public void functions(LinkedList<String> names) {

		names.add("antiesu");

	}

	@Override
	public int target() {

		return Group;

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
	
	static String[] keys = new String[]{
		
		"🐴","🐮","🍺","👊","震撼","废物","弱智","¿","96子","恁","魔怔","碰瓷","寻思","傻逼",
		
		"出道","hj","户籍","牛子","领证","野爹","夜蝶","这事","ao的",

		"迫真","察觉","无关心","便乘","棒读","谔谔","辱骂","好时代",
		
		"893","114","514","兄贵","姐贵","bb","仙贝","先辈",
		
		"草","恶臭","池沼","噔噔咚","心肺停止","激寒","雷普",
		
		"林檎","难视","人间之","并感","饼干","小鬼","震声",
		
		"直球","屑","鉴","野兽","一般通过","神必","削除","寻思",
		
		"杰哥","阿杰","如果早知道","不要啊","兄啊","高雅","正义",
	
		"，，+","野蛮","文明","大脑","最后警告","黑屁","确信",
		
		"创蜜","谢绝","创谢","创拜","创安","创不起","创哀","创持","已踢",
		
		"亲甜滴","喷香滴","创死我了","太创了","姥姥","啃","创象","人1",
		
		"自嘲完美","蛆","完美华丽","仏了","那(您|恁)","奇妙深刻","唐突","震撼",
		
		"操","实名",
		
	};
	
	static final String regex;
	
	static final String[] stickers = new String[] {
		
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
		
		"hanaakari"
		
	};
	
	static {
		
		StringBuilder kw = new StringBuilder(".*(");

		for (int index = 0;index < keys.length;index ++) {
			
			if (index == 0) kw.append(keys[0]);
			else kw.append("|").append(keys[index]);
			
		}
		
		kw.append(").*");
		
		regex = kw.toString();
		
	}
	
	public static boolean keywordMatch(String msg) {
		
		return msg.matches(regex);
		
	}

	@Override
	public boolean onGroup(UserData user,Msg msg) {
		
		if (!enable.contains(msg.chatId().longValue())) return false;
		
		if (msg.hasText() && msg.text().replace(" ","").matches(regex)) {
			
			msg.delete();
			
			return true;
			
		} else if (msg.message().sticker() != null) {
			
			if (ArrayUtil.contains(stickers, msg.message().sticker().setName())) {
				
				msg.delete();
				
				return true;
				
			}
			
		}
		
		return false;
		
	}
	
}
