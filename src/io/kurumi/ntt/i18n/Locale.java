package io.kurumi.ntt.i18n;

import io.kurumi.ntt.td.TdApi;

public class Locale {

	public static Locale DEFAULT = new Locale();
	public static Locale ENG = new ENG();

	public String PING_RESULT = "喵...";
	
	public String FN_PUBLIC_ONLY = "你只能在 群组或频道 使用这个命令.";
	public String NOT_CHAT_ADMIN = "你不是绒布球.";

	// DA

	public String CA_NOT_FOUND = "这个聊天中没有妇产账号.";
	public String CA_FOUND = "发现 {} 个目标账号, 正在清理.";
	public String CA_FINISH = "完成, 耗时 {}s.";

	// DNS
	
	public String DNS_TYPE_INVALID = "无效的DNS记录类型.";
	public String DNS_DOMAIN_INVALID = "无效的域名.";
	public String DNS_NOT_FOUND = "没有DNS记录.";
	
	public static class ENG extends Locale {

		{

			PING_RESULT = "pong";
			
			FN_PUBLIC_ONLY = "you can only use this command in groups or channels.";
			NOT_CHAT_ADMIN = "you are not the administrator of this chat.";

			CA_NOT_FOUND = "there is no target account in this chat.";
			CA_FOUND = "found {} target account(s) in this group, now removing ...";
			CA_FINISH = "successful , time : {}s";

			DNS_TYPE_INVALID = "Invalid dns type.";
			DNS_DOMAIN_INVALID = "Invalid domain.";
			DNS_NOT_FOUND = "Records not found.";
			
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
