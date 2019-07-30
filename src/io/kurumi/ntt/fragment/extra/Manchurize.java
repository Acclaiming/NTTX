package io.kurumi.ntt.fragment.extra;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;
import cn.hutool.core.util.ArrayUtil;
import io.kurumi.ntt.model.Query;
import io.kurumi.ntt.utils.Cndic;
import cn.hutool.core.util.StrUtil;
import java.util.regex.Pattern;
import cn.hutool.core.util.ReUtil;

/*

 作者 : https://github.com/OverflowCat

 源码 : https://github.com/OverflowCat/ManchuBot/blob/master/Manchurize.gs

 */
public class Manchurize extends Fragment {

	@Override
	public boolean query() {

		return true;

	}

	Cndic cndic = new Cndic();

	@Override
	public void onQuery(UserData user,Query inlineQuery) {

		if (inlineQuery.text == null || !inlineQuery.text.startsWith("M ")) return;

		String str = inlineQuery.text.substring(2).trim();

		if (StrUtil.isBlank(str)) return;

		str = cndic.cn_ma(isManchuScript(str) ? deManchurize(str) : str,isManchuScript(str),false);

		if (str != null) {

			inlineQuery.article("完成",manchurize(str),null,null);

		} else {

			inlineQuery.article("翻译失败",":(",null,null);


		}

		executeAsync(inlineQuery.update,inlineQuery.reply().cacheTime(114514));

	}

	private static Pattern manchuMatcher = Pattern.compile("(([\u1800-\u18AA\u00AB\u00BB\u2039\u203A\\?\\!\u203D\u2E2E])+\\s*((-*—?[0-9])+\\s+)*)+$",Pattern.MULTILINE);

	public static boolean isManchuScript(String str) {

		return ReUtil.contains(manchuMatcher,str);

	}

	public static String deManchurize(String str) {

		String tmp = "";

		if (str.length() > 0) {

			for (int i = 0; i < str.length(); i++) {

				char val = str.charAt(i);

				char prev = ' ';

				if (i > 0) {

					prev = str.charAt(i - 1);

				}

				if (val == 'ᠠ') {

					tmp += 'a';

				} else if (val == 'ᡝ') {

					tmp += 'e';

				} else if (val == 'ᡳ') {

					tmp += 'i';

				} else if (val == 'ᠣ') {

					tmp += 'o';

				} else if (val == 'ᡠ') {

					tmp += 'u';

				} else if (val == 'ᡡ') {

					tmp += 'v';

				} else if (val == '@') {

					tmp += 'ᡡ';

				} else if (val == 'ᠨ') {

					tmp += 'n';

				} else if (val == 'ᠩ') {

					tmp += 'N';

				} else if (val == 'ᠪ') {

					tmp += 'b';

				} else if (val == 'ᡦ') {

					tmp += 'p';

				} else if (val == 'ᡧ') {

					tmp += 'x';

				} else if (val == 'ᡧ') {

					tmp += 'S';

				} else if (val == 'ᡴ') {

					tmp += 'k';

				} else if (val == 'ᡤ' || val == 'ᠩ') {

					if (prev == 'ᠨ' || prev == 'n') {
						tmp = tmp.substring(0,tmp.length() - 1);
						tmp += 'ᠩ';
					} else {
						tmp += 'ᡤ';
					}

					tmp += 'g';

				} else if (val == 'ᡥ') {

					tmp += 'h';

				} else if (val == 'ᠮ') {

					tmp += 'm';

				} else if (val == 'ᠯ') {

					tmp += 'l';

				} else if (val == 'ᡨ') {

					tmp += 't';

				} else if (val == 'ᡩ') {

					tmp += 'd';

				} else if (val == 'ᠰ' || val == 'ᡮ') {

					if (prev == 'ᡨ' || prev == 't') {

						tmp = tmp.substring(0,tmp.length() - 1);

						tmp += 'ᡮ';

					} else {

						tmp += 'ᠰ';

					}

					tmp += 's';

				} else if (val == 'ᠴ') {

					tmp += 'c';

				} else if (val == 'ᠵ') {

					tmp += 'j';

				} else if (val == 'ᠶ') {

					tmp += 'y';

				} else if (val == 'ᡵ') {

					tmp += 'r';

				} else if (val == 'ᠸ') {

					tmp += 'w';

				} else if (val == 'ᡶ') {

					tmp += 'f';

				} else if (val == 'ᠺ') {

					tmp += 'K';

				} else if (val == 'ᡬ') {

					tmp += 'G';

				} else if (val == 'ᡭ') {

					tmp += 'H';

				} else if (val == 'ᡷ') {

					tmp += 'J';

				} else if (val == 'ᡱ') {

					tmp += 'C';

				} else if (val == 'ᡰ') {

					tmp += 'R';

				} else if (val == 'ᡯ') {

					// 'z') {
					if (prev == 'ᡩ' || prev == 'd') {

						tmp = tmp.substring(0,tmp.length() - 1);

						tmp += 'z';

					} else {

						tmp += 'z';

					}

				} else if (val == '\"') {

					tmp += '\u180B';

				} else if (val == '\u180C') {

					tmp += '\\';

				} else if (val == '\u180D') {

					tmp += '`';

				} else if (val == '\u180E') {

					tmp += '_';

				} else if (val == '\u202F') {

					tmp += '-';

				} else if (val == '\u200D') {

					tmp += '*';

				} else {

					tmp += val;

				}

			}

		}

		return tmp;

	}

	public static String manchurize(String str) {

		String tmp = "";

		if (str.length() > 0) {

			for (int i = 0; i < str.length(); i++) {

				char val = str.charAt(i);

				char prev = ' ';

				if (i > 0) {

					prev = str.charAt(i - 1);

				}

				if (val == 'a') {

					tmp += 'ᠠ';

				} else if (val == 'e') {

					tmp += 'ᡝ';

				} else if (val == 'i') {

					tmp += 'ᡳ';

				} else if (val == 'o') {

					tmp += 'ᠣ';

				} else if (val == 'u') {

					tmp += 'ᡠ';

				} else if (val == 'v') {

					tmp += 'ᡡ';

				} else if (val == '@') {

					tmp += 'ᡡ';

				} else if (val == 'n') {

					tmp += 'ᠨ';

				} else if (val == 'N') {

					tmp += 'ᠩ';

				} else if (val == 'b') {

					tmp += 'ᠪ';

				} else if (val == 'p') {

					tmp += 'ᡦ';

				} else if (val == 'x') {

					tmp += 'ᡧ';

				} else if (val == 'S') {

					tmp += 'ᡧ';

				} else if (val == 'k') {

					tmp += 'ᡴ';

				} else if (val == 'g') {

					if (prev == 'ᠨ' || prev == 'n') {

						tmp = tmp.substring(0,tmp.length() - 1);

						tmp += 'ᠩ';

					} else {

						tmp += 'ᡤ';

					}

				} else if (val == 'h') {

					tmp += 'ᡥ';

				} else if (val == 'm') {

					tmp += 'ᠮ';

				} else if (val == 'l') {

					tmp += 'ᠯ';

				} else if (val == 't') {

					tmp += 'ᡨ';

				} else if (val == 'd') {

					tmp += 'ᡩ';

				} else if (val == 's') {

					if (prev == 'ᡨ' || prev == 't') {

						tmp = tmp.substring(0,tmp.length() - 1);

						tmp += 'ᡮ';

					} else {

						tmp += 'ᠰ';

					}

				} else if (val == 'c') {

					tmp += 'ᠴ';

				} else if (val == 'j') {

					tmp += 'ᠵ';

				} else if (val == 'y') {

					tmp += 'ᠶ';

				} else if (val == 'r') {

					tmp += 'ᡵ';

				} else if (val == 'w') {

					tmp += 'ᠸ';

				} else if (val == 'f') {

					tmp += 'ᡶ';

				} else if (val == 'K') {

					tmp += 'ᠺ';

				} else if (val == 'G') {

					tmp += 'ᡬ';

				} else if (val == 'H') {

					tmp += 'ᡭ';

				} else if (val == 'J') {

					tmp += 'ᡷ';

				} else if (val == 'C') {

					tmp += 'ᡱ';

				} else if (val == 'R') {

					tmp += 'ᡰ';

				} else if (val == 'z') {

					if (prev == 'ᡩ' || prev == 'd') {

						tmp = tmp.substring(0,tmp.length() - 1);

						tmp += 'ᡯ';

					} else {

						tmp += 'z';

					}

				} else if (val == '\'') {

					tmp += '\u180B';

				} else if (val == '"') {

					tmp += '\u180C';

				} else if (val == '`') {

					tmp += '\u180D';

				} else if (val == '_') {

					tmp += '\u180E';

				} else if (val == '-') {

					tmp += '\u202F';

				} else if (val == '*') {

					tmp += '\u200D';

				} else {

					tmp += val;

				}

			}

		}

		return tmp;

	}

}
