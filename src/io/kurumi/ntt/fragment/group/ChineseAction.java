package io.kurumi.ntt.fragment.group;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.CharUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.json.JSONArray;
import io.kurumi.ntt.db.LocalData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.abs.Function;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.utils.NTT;

import java.security.acl.Group;
import java.util.LinkedList;

public class ChineseAction extends Function {

    public static JSONArray disable = LocalData.getJSONArray("data", "disable_action", true);
    public static ChineseAction INSTANCE = new ChineseAction();

    @Override
    public void functions(LinkedList<String> names) {

        names.add("action");

    }

    @Override
    public int target() {

        return Group;

    }

    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params) {

        if (NTT.checkGroupAdmin(msg)) return;

        if (params.length == 1 && "off".equals(params[0])) {

            if (disable.contains(msg.chatId().longValue())) {

                msg.send("无需重复关闭 ~").exec();

            } else {

                disable.add(msg.chatId());

                LocalData.setJSONArray("data", "disable_action", disable);

                msg.send("关闭成功 ~").exec();

            }

        } else {

            if (!disable.contains(msg.chatId().longValue())) {

                msg.send("没有关闭 ~").exec();

            } else {

                disable.remove(msg.chatId().longValue());

                LocalData.setJSONArray("data", "disable_action", disable);

                msg.send("已开启 ~").exec();

            }

        }


    }

    boolean startWithChinese(String msg) {

        if (msg == null) return false;

        if (msg.length() == 0) return false;

        char first = msg.charAt(0);

        return !CharUtil.isLetterOrNumber(first) && !CharUtil.isEmoji(first);

        // return first >= 0x4E00 &&  first <= 0x9FA5;

    }

    @Override
    public boolean onGroup(UserData user, Msg msg) {

        if (disable.contains(msg.chatId().longValue())) return false;

        if (startWithChinese(msg.command())) {

            if (msg.replyTo() != null) {

                if (msg.params().length > 0) {

                    String params = ArrayUtil.join(msg.params(), " ");

                    msg.send(user.userName() + " " + HtmlUtil.escape(msg.command()) + " " + msg.replyTo().from().userName() + " " + params + " ~").html().exec();

                } else {

                    msg.send(user.userName() + " " + HtmlUtil.escape(msg.command()) + "了 " + msg.replyTo().from().userName() + " ~").html().exec();

                }

            } else {

                String command = msg.params().length > 0 ? msg.command() + " " + ArrayUtil.join(msg.params(), " ") : msg.command();

                if (!command.contains("了")) command = command + "了";

                msg.send(user.userName() + " " + HtmlUtil.escape(command) + " ~").html().exec();

                msg.delete();

            }

            return true;

        }

        return false;

    }

}
