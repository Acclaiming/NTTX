package io.kurumi.ntt.i18n;

import io.kurumi.ntt.td.TdApi;

public class Locale {

	public static Locale DEFAULT = new Locale();
	public static Locale ENG = new ENG();

	public String FN_PUBLIC_ONLY = "你只能在 群组或频道 使用这个命令.";
	public String NOT_CHAT_ADMIN = "你不是绒布球.";

	// DA

	public String DA_NOT_FOUND = "这个 群组 / 频道 中没有已删除的账号.";
	public String DA_FOUND = "发现 {} 个已删除的账号, 正在清理.";
	public String DA_FINISH = "完成, 已清理 {}个 DA, 耗时 {}s.";

	public static class ENG extends Locale {

		{

			FN_PUBLIC_ONLY = "You can only use this command in groups or channels.";
			NOT_CHAT_ADMIN = "You are not the administrator of this chat.";

			DA_NOT_FOUND = "There is no deleted account in this chat.";
			DA_FOUND = "Found {} deleted account(s) in this group, now removing ...";
			DA_FINISH = "Successful removed {} deleted account(s), time : {}s";
			
		}

	}

	public static Locale get(TdApi.User user) {

		if (user.languageCode.contains("zh")) {

			return DEFAULT;

		} else {

			return ENG;

		}

	}

}
