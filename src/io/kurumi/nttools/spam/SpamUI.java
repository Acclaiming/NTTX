package io.kurumi.nttools.spam;

import io.kurumi.nttools.fragments.FragmentBase;
import io.kurumi.nttools.utils.UserData;
import io.kurumi.nttools.model.Msg;
import io.kurumi.nttools.model.request.ButtonMarkup;
import io.kurumi.nttools.model.request.ButtonLine;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import io.kurumi.nttools.model.Callback;
import org.w3c.dom.UserDataHandler;

public class SpamUI extends FragmentBase {

    public static final SpamUI INSTANCE = new SpamUI();

    public static String help = "/spam Twitter 联合封禁";

    private static final String POINT_BACK = "s|b";
    private static final String POINT_PUBLIC_LISTS = "s|p";
    private static final String POINT_NEW_LIST = "s|n";
    private static final String POINT_SHOW_LIST = "s|s";

    private static final String POINT_SHOW_SPAM_USERS = "s|u";

    private static final String POINT_EDIT_LIST_NAME = "s|en";
    private static final String POINT_EDIT_LIST_DESC = "s|ed";

    private static final String POINT_DELETE_LIST = "s|d";

    private static final String POINT_INPUT_LIST_NAME = "s|i";

    @Override
    public boolean processPrivateMessage(UserData user, Msg msg) {

        if (user.point != null) {

            switch (user.point.getPoint()) {

                    case POINT_INPUT_LIST_NAME : onInputListName(user, msg);break;
                    case POINT_EDIT_LIST_NAME : onEditListName(user, msg);break;
                    case POINT_EDIT_LIST_DESC : onEditListDesc(user, msg);break;
                    case POINT_DELETE_LIST : onConfirmDelete(user, msg);break;

                    default : return false;

            }

        } else {

            if (!msg.isCommand() || !"spam".equals(msg.commandName())) return false;

            sendMain(user, msg, false);

        }

        return true;

    }

    private void sendMain(final UserData user, Msg msg, boolean edit) {

        if (!edit) {

            deleteLastSend(user, msg, "spam_ui");

        }

        String[] spamMsg = new String[] {

            "「 Twitter 联合封禁 目录 」 :",

        };

        BaseResponse resp = sendOrEdit(msg, edit, spamMsg)

            .buttons(new ButtonMarkup() {{

                    newButtonLine("「公共分类列表」", POINT_PUBLIC_LISTS);

                }}).exec();

        saveLastSent(user, msg, "spam_ui", resp);

    }

    public void publicLists(final UserData user, final Msg msg, boolean edit) {

        if (!edit) {

            deleteLastSend(user, msg, "spam_ui");

        }

        BaseResponse resp = sendOrEdit(msg, edit, "这是公共分类列表 您可以选择开启、提交、申诉...")
            .buttons(new ButtonMarkup() {{

                    if (user.isAdmin) {

                        newButtonLine("「 管理员 : 新建分类 」", POINT_NEW_LIST);

                    }

                    for (SpamList spam : msg.fragment.main.getSpamLists()) {

                        newButtonLine("「 " + spam.name + " 」", POINT_SHOW_LIST, spam.id);

                    }

                    newButtonLine("「 返回目录 」 ", POINT_BACK);

                }}).exec();

        saveLastSent(user, msg, "spam_ui", resp);

    }

    public void newList(UserData user, Callback callback) {

        if (!user.isAdmin) { callback.alert("Error"); return; }

        user.point = cdata(POINT_INPUT_LIST_NAME);

        user.save();

        callback.text("好的");

        callback.send("输入列表的名称 (◦˙▽˙◦) : ").exec();

    }

    private void onInputListName(UserData user, Msg msg) {

        user.point = null;
        user.save();

        msg.fragment.main.newSpamList(msg.text());

        msg.send("创建成功 ⊙∀⊙ ~").exec();

        publicLists(user, msg, false);

    }

    public void showList(final UserData user, Msg msg, boolean edit, String spamId) {

        if (!edit) {

            deleteLastSend(user, msg, "spam_ui");

        }

        final SpamList spam = msg.fragment.main.getSpamList(spamId);

        BaseResponse resp = sendOrEdit(msg, edit, "查看公共分类 「 " + spam.name + " 」 (◦˙▽˙◦)", "", spam.description)
            .buttons(new ButtonMarkup() {{

                    if (user.isAdmin)  {

                        newButtonLine("「 管理员 - 修改名称 」", POINT_EDIT_LIST_NAME, spam.id);
                        newButtonLine("「 管理员 - 修改介绍 」", POINT_EDIT_LIST_DESC, spam.id);
                        newButtonLine("「 管理员 - 删除分类 」", POINT_DELETE_LIST, spam.id);

                    }

                    newButtonLine("「 查看所有分类用户 」", POINT_SHOW_SPAM_USERS, spam.id);

                    newButtonLine("「 返回分类列表 」", POINT_PUBLIC_LISTS);

                }}).exec();

        saveLastSent(user, msg, "spam_ui", resp);

    }

    private void editName(UserData user, Callback callback) {

        if (!user.isAdmin) { callback.alert("Error"); return; }

        user.point = cdata(POINT_EDIT_LIST_NAME, callback.data.getIndex());

        user.save();

        callback.send("输入新的名称 (｡>∀<｡) :").exec();

    }

    private void onEditListName(UserData user, Msg msg) {

        SpamList spam = msg.fragment.main.getSpamList(user.point.getIndex());

        spam.name = msg.text();

        spam.save();

        user.point = null;

        user.save();

        msg.send("修改成功 ~");

        showList(user, msg, false, spam.id);

    }

    private void editDesc(UserData user, Callback callback) {

        if (!user.isAdmin) { callback.alert("Error"); return; }

        user.point = cdata(POINT_EDIT_LIST_DESC, callback.data.getIndex());

        user.save();

        callback.send("输入新的简介 (｡>∀<｡) :").exec();

    }

    private void onEditListDesc(UserData user, Msg msg) {

        SpamList spam = msg.fragment.main.getSpamList(user.point.getIndex());

        spam.description = msg.text();

        spam.save();

        user.point = null;

        user.save();

        msg.send("修改成功 ~");

        showList(user, msg, false, spam.id);

    }

    private void deleteList(UserData user, Callback callback) {

        if (!user.isAdmin) { callback.alert("Error"); return; }

        user.point = cdata(POINT_DELETE_LIST, callback.data.getIndex());

        user.save();

        callback.send("输入 '确认删除这个列表' (简体字) 以确认删除 :", "", "取消删除使用 /cancel").exec();

    }

    private void onConfirmDelete(UserData user, Msg msg) {

        SpamList spam = msg.fragment.main.getSpamList(user.point.getIndex());

        if (!"确认删除这个列表".equals(msg.text())) {

            msg.send("您正在删除Twitter联合封禁列表 : " + spam.name, "输入 '确认删除这个列表' (简体字) 以确认删除 :", "", "取消删除使用 /cancel").exec();

        } else {

            spam = msg.fragment.main.deleteSpamList(spam.id);

            msg.send("联合封禁列表 : 「 " + spam.name + " 」 已删除 ~").exec();

            user.point = null;

            user.save();

            publicLists(user, msg, false);


        }


    }


    @Override
    public boolean processCallbackQuery(UserData user, Callback callback) {

        switch (callback.data.getPoint()) {

                case POINT_PUBLIC_LISTS : publicLists(user, callback, true);break;
                case POINT_NEW_LIST : newList(user, callback);break;
                case POINT_SHOW_LIST : showList(user, callback, true, callback.data.getIndex());break;

                case POINT_EDIT_LIST_NAME : editName(user, callback);break;
                case POINT_EDIT_LIST_DESC : editDesc(user, callback);break;

                case POINT_DELETE_LIST : deleteList(user, callback);break;

                case POINT_BACK : sendMain(user, callback, true);break;

                default : return false;

        }

        return true;

    }

}
