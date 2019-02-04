package io.kurumi.nttools.spam;

import com.pengrad.telegrambot.response.BaseResponse;
import io.kurumi.nttools.fragments.FragmentBase;
import io.kurumi.nttools.model.Callback;
import io.kurumi.nttools.model.Msg;
import io.kurumi.nttools.model.request.ButtonMarkup;
import io.kurumi.nttools.utils.Markdown;
import io.kurumi.nttools.utils.UserData;
import twitter4j.User;

public class SpamUI extends FragmentBase {

    public static final SpamUI INSTANCE = new SpamUI();

    public static String help = "/spam Twitter 联合封禁";

    private static final String POINT_BACK = "s|b";
    private static final String POINT_PUBLIC_LISTS = "s|p";
    private static final String POINT_NEW_LIST = "s|n";
    private static final String POINT_SHOW_LIST = "s|s";

    private static final String POINT_SHOW_SPAM_USERS = "s|u";

    private static final String POINT_NEW_SPAM = "s|ns";
    private static final String POINT_ADD_SPAM = "s|a";

    private static final String POINT_EDIT_LIST_NAME = "s|e";
    private static final String POINT_EDIT_LIST_DESC = "s|ed";

    private static final String POINT_DELETE_LIST = "s|d";

    private static final String POINT_INPUT_LIST_NAME = "s|il";
    private static final String POINT_INPUT_SCREEN_NAME= "s|is";
    private static final String POINT_INPUT_CAUSE = "s|ic";

    @Override
    public boolean processPrivateMessage(UserData user, Msg msg) {

        if (user.point != null) {

            switch (user.point.getPoint()) {

                    case POINT_INPUT_LIST_NAME : onInputListName(user, msg);break;
                    case POINT_EDIT_LIST_NAME : onEditListName(user, msg);break;
                    case POINT_EDIT_LIST_DESC : onEditListDesc(user, msg);break;
                    case POINT_DELETE_LIST : onConfirmDelete(user, msg);break;

                    case POINT_INPUT_SCREEN_NAME : onInputScreenName(user, msg);break;
                    case POINT_INPUT_CAUSE : onInputCause(user, msg);break;


                    default : return false;

            }

        } else {

            if (!msg.isCommand()) return false;

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

        callback.send("输入列表的名称 (◦˙▽˙◦) : ", "", "使用 /cancel 取消创建 ~").exec();

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

                        //     newButtonLine("「 管理员 - 设为向官方举报的列表 」", POINT_DELETE_LIST, spam.id);

                    }

                    newButtonLine("「 查看所有分类用户 」", POINT_SHOW_SPAM_USERS, spam.id);

                    newButtonLine("「 返回分类列表 」", POINT_PUBLIC_LISTS);

                }}).exec();

        saveLastSent(user, msg, "spam_ui", resp);

    }

    private void editName(UserData user, Callback callback) {

        if (!user.isAdmin) { callback.alert("Error"); return; } else callback.confirm();

        user.point = cdata(POINT_EDIT_LIST_NAME, callback.data.getIndex());

        user.save();

        callback.send("输入新的名称 (｡>∀<｡) :", "", "使用 /cancel 取消修改 ~").exec();

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

        if (!user.isAdmin) { callback.alert("Error"); return; } else callback.confirm();

        user.point = cdata(POINT_EDIT_LIST_DESC, callback.data.getIndex());

        user.save();

        callback.send("输入新的简介 (｡>∀<｡) :", "", "使用 /cancel 取消修改 ~").exec();

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

        if (!user.isAdmin) { callback.alert("Error"); return; } else callback.confirm();

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

    private void showListSpams(UserData user, Callback callback) {

        final SpamList list = callback.fragment.main.getSpamList(callback.data.getIndex());

        StringBuilder all = new StringBuilder("公共列表 「 " + list.name + " 」 的所有内容 :\n\n");

        for (UserSpam spam : list.spamUsers) {

            all.append("[").append(Markdown.encode(spam.twitterDisplyName)).append("](https://twitter.com/").append(spam.twitterScreenName).append(")");

            if (spam.vote_message_id != null) {

                all.append(" [投票地址](https://t.me/").append(TwitterSpam.VOTE_CHANNEL).append("/").append(spam.vote_message_id).append(")");

            }

            all.append("\n");

        }

        callback.edit(all.toString()).buttons(new ButtonMarkup() {{

                    newButtonLine("「 发起新投票 」", POINT_NEW_SPAM, list.id);
                    newButtonLine("「 管理员 - 直接添加 」", POINT_ADD_SPAM, list.id);
                    newButtonLine("<< 返回列表", POINT_SHOW_LIST, list.id);

                }}).exec();

    }

    public void newSpamRequest(UserData user, Callback callback, boolean direct) {

        user.point = cdata(POINT_INPUT_SCREEN_NAME);

        user.point.put("listId", callback.data.getIndex());
        user.point.put("direct", direct);

        user.save();

        callback.confirm();

        callback.send("现在发送给我目标的TwitterId @开头 (也可以不带@)", "", "使用 /cancel 取消 >_<").exec();

    }

    public void  onInputScreenName(UserData user, Msg msg) {

        String screenName = msg.text();

        if (screenName.startsWith("@")) {

            screenName = screenName.substring(1);

        }

        try {

            User target = user.twitterAccounts.getFirst().createApi().showUser(screenName);

            Long accountId = target.getId();

            String displayName = target.getName();

            user.point.setPoint(POINT_INPUT_CAUSE);

            user.point.put("accountId", accountId);

            user.point.put("screenName", screenName);

            user.point.put("displayName", displayName);

            user.save();

            msg.send("好，现在输入理由 : ( 尽量详细 )").exec();

        } catch (Exception ex) {

            msg.send("找不到用户 https://twitter.com/" + screenName + " 或您的Twitter账号认证被取消了...", "", "请重新发送用户名 取消使用 /cancel (Ｔ▽Ｔ)").exec();

        }

    }

    public void onInputCause(UserData user, Msg msg) {

        SpamList list = msg.fragment.main.getSpamList(user.point.getStr("listId"));

        Long accountId = user.point.getLong("accountId");

        String screenName = user.point.getStr("screenName");

        String displayName = user.point.getStr("displayName");

        if (user.isAdmin && user.point.getBool("direct")) {
            
            UserSpam spam = new UserSpam(list);
            
            spam.origin = user.id;
            
            spam.twitterAccountId = accountId;

            spam.twitterScreenName = screenName;
            
            spam.twitterDisplyName = displayName;
            
            spam.spamCause = msg.text();
            
            list.spamUsers.add(spam);
            
            list.save();
            
            msg.fragment.main.spam.newSpam(spam);
            
            msg.send("添加成功 ~").exec();
            
        } else {
            
            SpamVote spam = msg.fragment.main.newSpamVote(user.id,accountId,screenName,displayName,msg.text());
            
            VoteUI.INSTANCE.startVote(msg.fragment,spam);
            
            msg.send("发起投票成功 ~").exec();
            
        }
        
        user.point = null;
        user.save();

    }

    @Override
    public boolean processCallbackQuery(UserData user, Callback callback) {

        switch (callback.data.getPoint()) {

                case POINT_PUBLIC_LISTS : publicLists(user, callback, true);callback.confirm();break;
                case POINT_NEW_LIST : newList(user, callback);callback.confirm();break;
                case POINT_SHOW_LIST : showList(user, callback, true, callback.data.getIndex());callback.confirm();break;

                case POINT_SHOW_SPAM_USERS : showListSpams(user, callback);break;

                case POINT_NEW_SPAM : newSpamRequest(user, callback, false);break;
                case POINT_ADD_SPAM : newSpamRequest(user, callback, true);break;

                case POINT_EDIT_LIST_NAME : editName(user, callback);break;
                case POINT_EDIT_LIST_DESC : editDesc(user, callback);break;

                case POINT_DELETE_LIST : deleteList(user, callback);break;

                case POINT_BACK : sendMain(user, callback, true);callback.confirm();break;

                default : return false;

        }

        return true;

    }

}
