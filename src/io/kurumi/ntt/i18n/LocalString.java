package io.kurumi.ntt.i18n;

import cn.hutool.core.util.StrUtil;
import io.kurumi.ntt.db.GroupData;
import io.kurumi.ntt.db.UserData;

public class LocalString {

    public static LocalString DEFAULT = new LocalString();

    public String LANG_NAME = "简体中文 (默认)";

    public String CANCEL = "已重置当前会话状态 .";

    public String FORCE_CANCEL = "因为NTT出错 / 更新 , 即将重启 , 已经重置当前会话状态 .";

    public String COMMAND_GROUP_ONLY = "该命令必须在群组使用 :)";

    public String COMMAND_PRIVATE_ONLY = "该命令必须在私聊使用 :)";

    public String PING_RESULT = "喵 ....";

    public String UNPROCESSED = "喵......？";

    public String INPUT = "请输入 ";

    // TWITTER

    public String TWITTER_AUTH_NEED = "这个命令 认证Twitter账号之后 才能使用 ( 使用 /twitter 管理账号 )";

    public String TWITTER_INVALID_ACCOUNT = "无效的账号.";

    public String TWITTER_NEW_AUTH = " >> 认证账号 <<";

    public String TWITTER_CHOOSE_ACCOUNT = ">> 选择一个账号查看设定 <<";

    public String TWITTER_NO_ACCOUNT = ">> 还没有认证账号 <<";

    public String TWITTER_AUTH_API = "请选择接口 (发送的推文下方显示的来源)";

    public String TWITTER_AUTH_CUSTOM = "第三方接口";

    public String TWITTER_AUTH_IMPORT = "导入认证";

    public String TWITTER_REQEUST_AUTH_FAILED = "请求认证链接失败 :(";

    public String TWITTER_AUTH_LINK = "点击链接认证 : ";

    public String TWITTER_AUTH_PIN = "请输入PIN 码 : ";

    public String TWITTER_AUTHED_BY_OTHER = "你的账号 {} 已被 {} 认证 , 已移除 . ";

    public String twitterAuthedByOther(String account, String user) {

        return StrUtil.format(TWITTER_AUTHED_BY_OTHER, account, user);

    }

    public String TWITTER_AUTH_FAILED = "认证失败...";

    public String TWITTER_AUTH_EXPORT = "导出";

    public String TWITTER_AUTH_REMOVE = "移除";

    public static LocalString get(GroupData group) {

        return DEFAULT;

    }

    public static LocalString get(UserData user) {

        return DEFAULT;

    }

}
