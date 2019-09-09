package io.kurumi.ntt.fragment.group;

import io.kurumi.ntt.fragment.Fragment;

public class AntiEsu extends Fragment {

		/*
		
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

		
		static HanyuPinyinOutputFormat format;

    static {

				format = new HanyuPinyinOutputFormat();

				format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
				format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
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
		public int checkFunctionContext(UserData user,Msg msg,String function,String[] params) {

				return FUNCTION_GROUP;

		}

		@Override
		public int checkMsg(UserData user,Msg msg) {

				/*

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
		
		*/

}
