package io.kurumi.ntt.fragment.group.options;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import com.pengrad.telegrambot.model.Sticker;
import com.pengrad.telegrambot.request.GetStickerSet;
import com.pengrad.telegrambot.response.GetStickerSetResponse;
import io.kurumi.ntt.db.GroupData;
import io.kurumi.ntt.db.PointData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.ButtonMarkup;

import java.util.LinkedList;

public class WelcomeMain extends Fragment {

    public static String POINT_WELCOME = "group_shoe";

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerCallback(POINT_WELCOME);
        registerPoint(POINT_WELCOME);

    }

    @Override
    public void onCallback(UserData user, Callback callback, String point, String[] params) {

        if (params.length == 0 || !NumberUtil.isNumber(params[0])) {

            callback.invalidQuery();

            return;

        }

        final GroupData data = GroupData.data.getById(NumberUtil.parseLong(params[0]));

        if (data == null) {

            callback.invalidQuery();

            return;

        }

        if (params.length == 1) {

            callback.edit(showStats(data)).buttons(showMenu(data)).async();

            return;

        }

        if ("show_disable".equals(params[1])) {

            data.welcome = null;

            callback.text("📢  已关闭");

        } else if ("show_text".equals(params[1])) {

            if (data.welcomeMessage == null) {

                callback.alert("文本内容未设定");

                return;

            }

            data.welcome = 0;

            callback.text("📢  文本欢迎消息");

        } else if ("show_sticker".equals(params[1])) {

            if (data.welcomeSet == null) {

                callback.alert("贴纸未设定");

                return;

            }

            data.welcome = 1;

            callback.text("📢  贴纸欢迎消息");

        } else if ("text_and_sticker".equals(params[1])) {

            if (data.welcomeMessage == null) {

                callback.alert("文本内容未设定");

                return;

            } else if (data.welcomeSet == null) {

                callback.alert("贴纸未设定");

                return;

            }

            data.welcome = 2;

            callback.text("📢  贴纸与文本");

        } else if ("set_msg".equals(params[1])) {

            callback.confirm();

            EditCustom edit = new EditCustom(0, callback, data);

            callback.send("现在发送欢迎文本 :").exec(edit);

            setPrivatePoint(user, POINT_WELCOME, edit);


        } else if ("set_set".equals(params[1])) {

            callback.confirm();

            EditCustom edit = new EditCustom(1, callback, data);

            callback.send("现在发送贴纸来设定\n注意 : 如果发送贴纸包链接，则每次随机一张作为欢迎信息").exec(edit);

            setPrivatePoint(user, POINT_WELCOME, edit);


        } else if ("del_welcome".equals(params[1])) {

            if (data.del_welcome_msg == null) {

                data.del_welcome_msg = true;

                callback.text("📢  全部保留");

            } else {

                data.del_welcome_msg = null;

                callback.text("📢  保留最后一条");

            }

        }

        callback.edit(showStats(data)).buttons(showMenu(data)).async();


    }

    @Override
    public void onPoint(UserData user, Msg msg, String point, PointData data) {

        EditCustom edit = (EditCustom) data.with(msg);

        if (edit.type == 0) {

            if (!msg.hasText()) {

                msg.send("请发送欢迎文本").withCancel().exec(data);

                return;

            }

            edit.data.welcomeMessage = HtmlUtil.escape(msg.text());

            clearPrivatePoint(user);

        } else if (edit.type == 1) {

            if (msg.sticker() != null) {

                edit.data.welcomeSet = new LinkedList<>();

                edit.data.welcomeSet.add(msg.sticker().fileId());

            } else if (!msg.hasText()) {

                msg.send("请发送欢迎贴纸").withCancel().exec(data);

                return;

            } else {

                String target = msg.text();

                if (target.contains("/")) target = StrUtil.subAfter(target, "/", true);

                final GetStickerSetResponse set = bot().execute(new GetStickerSet(target));

                if (!set.isOk()) {

                    msg.send("无法读取贴纸包 " + target + " : " + set.description()).exec(data);

                    return;

                }

                edit.data.welcomeSet = new LinkedList<>();

                for (Sticker sticker : set.stickerSet().stickers()) edit.data.welcomeSet.add(sticker.fileId());

            }

            clearPrivatePoint(user);

        }

    }

    class EditCustom extends PointData {

        int type;
        Callback origin;
        GroupData data;

        public EditCustom(int type, Callback origin, GroupData data) {

            this.type = type;
            this.origin = origin;
            this.data = data;

        }

        @Override
        public void onFinish() {

            super.onFinish();

            origin.edit(showStats(data)).buttons(showMenu(data)).async();


        }

    }

    String showStats(GroupData data) {

        StringBuilder stats = new StringBuilder();

        stats.append("设置欢迎消息，BOT将在新成员加入时发送\n\n如果开启了加群验证，则在通过验证后发送\n\n如果没有开启 '删除服务消息' 则将直接对加群消息回复。");

        stats.append("\n\n欢迎消息 : ");

        if (data.welcomeMessage == null) {

            stats.append("未设定");

        } else {

            stats.append(HtmlUtil.escape(data.welcomeMessage));

        }

        stats.append("\n欢迎贴纸 : ");

        if (data.welcomeSet == null) {

            stats.append("未设定");

        } else {

            stats.append("已设定 ").append(data.welcomeSet.size()).append(" 张");

        }

        return stats.toString();

    }

    ButtonMarkup showMenu(final GroupData data) {

        return new ButtonMarkup() {{

            newButtonLine()
                    .newButton("关闭欢迎消息")
                    .newButton(data.welcome == null ? "●" : "○", POINT_WELCOME, data.id, "show_disable");

            newButtonLine()
                    .newButton("文本消息")
                    .newButton(((Integer) 0).equals(data.welcome) ? "●" : "○", POINT_WELCOME, data.id, "show_text");

            newButtonLine()
                    .newButton("贴纸消息")
                    .newButton(((Integer) 1).equals(data.welcome) ? "●" : "○", POINT_WELCOME, data.id, "show_sticker");

            newButtonLine()
                    .newButton("文本与贴纸")
                    .newButton(((Integer) 2).equals(data.welcome) ? "●" : "○", POINT_WELCOME, data.id, "text_and_sticker");

            newButtonLine("设置欢迎文本", POINT_WELCOME, data.id, "set_msg");
            newButtonLine("设置欢迎贴纸", POINT_WELCOME, data.id, "set_set");

            newButtonLine()
                    .newButton("仅保留最后一条")
                    .newButton(data.del_welcome_msg != null ? "✅" : "☑", POINT_WELCOME, data.id, "del_welcome");

            newButtonLine("🔙", OptionsMain.POINT_OPTIONS, data.id);

        }};

    }


}
