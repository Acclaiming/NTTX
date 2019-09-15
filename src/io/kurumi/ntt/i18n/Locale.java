package io.kurumi.ntt.i18n;

import io.kurumi.ntt.td.TdApi;
import io.kurumi.ntt.utils.Html;

public class Locale {

    public static Locale DEFAULT = new Locale();
    public static Locale ENG = new ENG();

    public String PING_RESULT = "喵...";

    public String FN_PUBLIC_ONLY = "你只能在 群组或频道 使用这个命令.";
    public String FN_GROUP_ONLY = "你只能在 群组 使用这个命令.";
    public String FN_CHANNEL_ONLY = "你只能在 频道 使用这个命令.";
    public String FN_PRIVATE_ONLY = "你只能在 私聊 使用这个命令.";
    public String NOT_CHAT_ADMIN = "你不是绒布球.";

    // CA

    public String CA_HELP = "<b>群组/频道清理成员</b> : " + "\n\n" +

            "清理已删除的账号 : /clean_da" + "\n" +

            "清理所有成员 : /clean_all" + "\n\n" +

            "注意 : <b>必须由管理员在群组中使用命令执行</b> 且需要 <b>限制成员</b> 权限";

    public String CA_NOT_FOUND = "这个聊天中没有目标账号.";
    public String CA_FOUND = "发现 {} 个目标账号, 正在清理.";
    public String CA_FINISH = "完成, 耗时 {}s.";

    // DNS

    public String DNS_TYPE_INVALID = "无效的 " + Html.a("DNS记录类型", "https://zh.wikipedia.org/wiki/%E5%9F%9F%E5%90%8D%E4%BC%BA%E6%9C%8D%E5%99%A8%E8%A8%98%E9%8C%84%E9%A1%9E%E5%9E%8B%E5%88%97%E8%A1%A8") + " .";
    public String DNS_DOMAIN_INVALID = "无效的 " + Html.a("域名", "https://zh.m.wikipedia.org/zh-cn/%E5%9F%9F%E5%90%8D") + " .";
    public String DNS_NOT_FOUND = "没有对应的DNS记录.";

    // USER

    public String GET_USER = "/get_user 对消息回复 / 文本引用 / 用户ID";

    public String GET_USER_NOT_FOUND = "这个用户不存在";

    public static class ENG extends Locale {
        {

            PING_RESULT = "pong";

            FN_PUBLIC_ONLY = "you can only use this command in groups or channels.";
            FN_GROUP_ONLY = "you can only use this command in groups.";
            FN_PRIVATE_ONLY = "you can only use this command in private messages.";
            NOT_CHAT_ADMIN = "you are not the administrator of this chat.";

            CA_HELP = "Clean up group members :" + "\n\n" +

                    "remove deleted accounts : /clean_da" + "\n" +

                    "remove all members : /clean_all" + "\n\n" +

                    "This command requires <b>Restrict Member</b> permission.";

            CA_NOT_FOUND = "there is no target account in this chat.";
            CA_FOUND = "found {} target account(s) in this group, now removing ...";
            CA_FINISH = "successful , time : {}s";

            DNS_TYPE_INVALID = "invalid dns type.";
            DNS_DOMAIN_INVALID = "Invalid domain.";
            DNS_NOT_FOUND = "records not found.";

            GET_USER = "/get_user reply to a message / text mention / user id";

            GET_USER_NOT_FOUND = "user not found.";

        }

    }

    public static Locale get(TdApi.User user) {

        if (user != null && user.languageCode.contains("zh")) {

            return DEFAULT;

        } else {

            return ENG;

        }

    }

}
