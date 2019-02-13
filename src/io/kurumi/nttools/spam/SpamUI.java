package io.kurumi.nttools.spam;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONObject;
import com.pengrad.telegrambot.response.BaseResponse;
import io.kurumi.nttools.fragments.FragmentBase;
import io.kurumi.nttools.model.Callback;
import io.kurumi.nttools.model.Msg;
import io.kurumi.nttools.model.request.ButtonLine;
import io.kurumi.nttools.model.request.ButtonMarkup;
import io.kurumi.nttools.twitter.TApi;
import io.kurumi.nttools.twitter.TwiAccount;
import io.kurumi.nttools.utils.Markdown;
import io.kurumi.nttools.utils.UserData;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
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
    private static final String POINT_REM_SPAM = "s|r";

    private static final String POINT_ADD_LIST = "s|al";
    private static final String POINT_CLEAR = "s|cn";

    private static final String POINT_MANAGE_VOTE = "s|m";

    private static final String POINT_EDIT_LIST_NAME = "s|e";
    private static final String POINT_EDIT_LIST_DESC = "s|ed";

    private static final String POINT_DELETE_LIST = "s|d";

    private static final String POINT_INPUT_LIST_NAME = "s|il";
    private static final String POINT_INPUT_SCREEN_NAME= "s|is";
    private static final String POINT_INPUT_CAUSE = "s|ic";
    private static final String POINT_INPUT_LIST_CAUSE = "s|ilc";

    private static final String POINT_OPEN = "s|o";
    private static final String POINT_CLOSE = "s|c";

    @Override
    public boolean processPrivateMessage(UserData user, Msg msg,boolean point) {

        if (user.point != null) {

            switch (user.point.getPoint()) {

                    case POINT_INPUT_LIST_NAME : onInputListName(user, msg);break;
                    case POINT_EDIT_LIST_NAME : onEditListName(user, msg);break;
                    case POINT_EDIT_LIST_DESC : onEditListDesc(user, msg);break;
                    case POINT_DELETE_LIST : onConfirmDelete(user, msg);break;

                    case POINT_INPUT_SCREEN_NAME : onInputScreenName(user, msg);break;
                    case POINT_INPUT_CAUSE : onInputCause(user, msg);break;

                    case POINT_CLEAR : onConfirmClearList(user, msg);break;

                    case POINT_INPUT_LIST_CAUSE : onInputListCause(user, msg);break;
                    case POINT_ADD_LIST : onInputCsv(user, msg);break;

                    default : return false;

            }

        } else {

            if (!msg.isCommand()) return false;

            switch (msg.commandName()) {

                    case "start" : parsePayload(user, msg);break;
                    case "spam" : sendMain(user, msg, false);break;

                    default : return false;

            }

        }

        return true;

    }

    private void sendMain(final UserData user, Msg msg, boolean edit) {

        if (!edit) {

            deleteLastSend(user, msg, "spam_ui");

        }

        if (user.twitterAccounts.isEmpty()) {

            msg.send("联合封禁使用Twitter以发起申请等...", "", "请先在 /twitter 认证账号 (｡>∀<｡)").exec();

            return;

        }

        String[] spamMsg = new String[] {

            "「 Twitter 联合封禁 目录 」 :",


        };

        BaseResponse resp = sendOrEdit(msg, edit, spamMsg)

            .buttons(new ButtonMarkup() {{

                    newButtonLine("「 公共分类列表 」", POINT_PUBLIC_LISTS);
                    newButtonLine()
                        .newUrlButton("「 投票频道 」", "https://t.me/" + TwitterSpam.VOTE_CHANNEL)
                        .newUrlButton("「 操作公开 」", "https://t.me/" + TwitterSpam.PUBLIC_CHANNEL);

                    newUrlButtonLine("「 加入公式群 」", "https://t.me/joinchat/H5gBQ1N2Mx5gf3Jm1e6RgQ");

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

                        newButtonLine("「 新建分类 」", POINT_NEW_LIST);

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

                        newButtonLine()
                            .newButton("「 修改名称 」", POINT_EDIT_LIST_NAME, spam.id)
                            .newButton("「 修改介绍 」", POINT_EDIT_LIST_DESC, spam.id)
                            .newButton("「 删除分类 」", POINT_DELETE_LIST, spam.id);

                        //     newButtonLine("「 管理员 - 设为向官方举报的列表 」", POINT_DELETE_LIST, spam.id);

                    }

                    newButtonLine("「 查看所有分类中的用户 」", POINT_SHOW_SPAM_USERS, spam.id);

                    ButtonLine voteLine = newButtonLine();

                    voteLine.newButton("「 发起新投票 」", POINT_NEW_SPAM, spam.id);

                    if (user.isAdmin) {

                        //     voteLine.newButton("「 管理投票 」", POINT_MANAGE_VOTE, spam.id);

                        newButtonLine()
                            .newButton("「 添加 」", POINT_ADD_SPAM, spam.id)
                            .newButton("「 解除 」", POINT_REM_SPAM, spam.id);
                        newButtonLine()
                            .newButton("「 批量添加 」", POINT_ADD_LIST, spam.id)
                            .newButton("「 清空 」", POINT_CLEAR, spam.id);

                    }

                    for (TwiAccount account : user.twitterAccounts) {

                        if (spam.disables.containsKey(account.accountId)) {

                            newButtonLine(account.name + " 「启用该列表」", POINT_OPEN, spam.id, user, account);

                        } else {

                            newButtonLine(account.name + " 「禁用该列表」", POINT_CLOSE, spam.id, user, account);


                        }

                    }

                    newButtonLine("「 返回分类列表 」", POINT_PUBLIC_LISTS);

                }}).exec();

        saveLastSent(user, msg, "spam_ui", resp);

    }

    private void enable(UserData user, Callback callback) {

        callback.text("开启成功 ~");

        SpamList list = callback.fragment.main.getSpamList(callback.data.getIndex());

        TwiAccount account = callback.data.getUser(user);

        list.disables.remove(account.accountId);

        list.save();

        showList(user, callback, true, list.id);

    }

    private void disable(UserData user, Callback callback) {

        callback.text("关闭成功 ~");

        SpamList list = callback.fragment.main.getSpamList(callback.data.getIndex());

        TwiAccount account = callback.data.getUser(user);

        list.disables.put(account.accountId,user.id);

        list.save();

        showList(user, callback, true, list.id);

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

        msg.send("修改成功 ~").exec();

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

        msg.send("修改成功 ~").exec();

        showList(user, msg, false, spam.id);

    }

    private void clearList(UserData user, Callback callback) {

        if (!user.isAdmin) { callback.alert("Error"); return; } else callback.confirm();

        user.point = cdata(POINT_CLEAR, callback.data.getIndex());

        user.save();

        callback.send("输入 '确认清空' (简体字) 以确认清空 :", "", "取消清空使用 /cancel").exec();

    }

    private void onConfirmClearList(UserData user, Msg msg) {

        SpamList spam = msg.fragment.main.getSpamList(user.point.getIndex());

        if (!"确认清空".equals(msg.text())) {

            msg.send("您正在清空Twitter联合封禁列表 : " + spam.name, "输入 '确认清空' (简体字) 以确认清空 :", "", "取消清空使用 /cancel").exec();

        } else {

            spam.spamUsers.clear();
            spam.save();

            msg.send("联合封禁列表 : 「 " + spam.name + " 」 已清空 ~").exec();

            user.point = null;

            user.save();

            showList(user, msg, false, spam.id);


        }


    }


    private void deleteList(UserData user, Callback callback) {

        if (!user.isAdmin) { callback.alert("Error"); return; } else callback.confirm();

        user.point = cdata(POINT_DELETE_LIST, callback.data.getIndex());

        user.save();

        callback.send("输入 '确认删除' (简体字) 以确认删除 :", "", "取消删除使用 /cancel").exec();

    }

    private void onConfirmDelete(UserData user, Msg msg) {

        SpamList spam = msg.fragment.main.getSpamList(user.point.getIndex());

        if (!"确认删除".equals(msg.text())) {

            msg.send("您正在删除Twitter联合封禁列表 : " + spam.name, "输入 '确认删除' (简体字) 以确认删除 :", "", "取消删除使用 /cancel").exec();

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

            all.append("[").append(Markdown.encode(spam.twitterDisplyName)).append("](");

            all.append("https://t.me/").append(TwitterSpam.PUBLIC_CHANNEL).append("/").append(spam.public_message_id).append(")");

            all.append("\n");

        }

        callback.edit(all.toString()).buttons(new ButtonMarkup() {{
                    newButtonLine("<< 返回列表", POINT_SHOW_LIST, list.id);

                }}).markdown().disableLinkPreview().exec();

    }

    public void newSpamRequest(UserData user, Callback callback, boolean direct , boolean remove) {

        user.point = cdata(POINT_INPUT_SCREEN_NAME);

        user.point.put("listId", callback.data.getIndex());
        user.point.put("direct", direct);
        user.point.put("remove", remove);

        user.save();

        callback.confirm();

        callback.send("输入TwitterId (可以带@)", "", "使用 /cancel 取消 >_<").exec();

    }

    public void  onInputScreenName(UserData user, Msg msg) {

        String screenName = msg.text();

        if (screenName.startsWith("@")) {

            screenName = screenName.substring(1);

        }

        if (!user.point.getBool("remove")) {

            SpamList list = msg.fragment.main.getSpamList(user.point.getStr("listId"));

            for (UserSpam spam : list.spamUsers) {

                if (screenName.equals(spam.twitterScreenName)) {

                    msg.send("该用户已在公共分类 「 " + list.name + " 」 中！", "", "请重新输入 或使用 /cancel 取消").exec();

                    return;

                }

            }

            for (SpamVote vote : msg.fragment.main.getSpamVotes()) {

                if (screenName.equals(vote.twitterScreenName) && !user.point.getBool("direct")) {

                    msg.send("该用户已经被提交！", "正在 [这里](https://t.me/" + TwitterSpam.VOTE_CHANNEL + "/" + vote.vote_message_id + ") 投票"  , "", "请重新输入 或使用 /cancel 取消").markdown().disableLinkPreview().exec();

                    return;

                }

            }

        } else {

            SpamList list = msg.fragment.main.getSpamList(user.point.getStr("listId"));

            boolean exists = false;

            for (UserSpam spam : list.spamUsers) {

                if (screenName.equals(spam.twitterScreenName)) {

                    exists = true;

                }

            }

            if (!exists && !user.point.getBool("direct")) {

                msg.send("该用户不在公共分类 「 " + list.name + " 」 中！", "", "请重新输入 或使用 /cancel 取消").exec();

                return;

            }

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

        SpamVote vote = null;

        for (SpamVote v : msg.fragment.main.getSpamVotes()) {

            if (screenName.equals(v.twitterScreenName)) {

                vote = v;

            }

        }

        if (user.isAdmin && user.point.getBool("remove")) {

            Iterator<UserSpam> i = list.spamUsers.iterator();

            while (i.hasNext()) {

                UserSpam spam = i.next();

                if (accountId.equals(spam.twitterAccountId)) {

                    i.remove();

                    msg.fragment.main.spam.remSpam(user, spam, msg.text());

                }

            }

            if (vote != null) {

                msg.fragment.main.spam.adminRejected(user, vote, msg.text());

            }

        } else if (user.isAdmin && user.point.getBool("direct")) {

            if (vote != null) {

                msg.fragment.main.spam.adminPassed(user, vote, msg.text());

            } else {

                UserSpam spam = new UserSpam(list);

                spam.origin = user.id;

                spam.twitterAccountId = accountId;

                spam.twitterScreenName = screenName;

                spam.twitterDisplyName = displayName;

                spam.spamCause = msg.text();

                final String url = msg.fragment.main.spam.newSpam(list, spam);

                msg.send("添加成功 ~").buttons(new ButtonMarkup() {{

                            newUrlButtonLine("公开地址", url);

                        }}).exec();

            }

        } else {

            SpamVote spam = msg.fragment.main.newSpamVote(list, user.id, accountId, screenName, displayName, msg.text());

            VoteUI.INSTANCE.startVote(msg.fragment, spam);

            final SpamVote v = vote;

            msg.send("发起投票成功 ~")
                .buttons(new ButtonMarkup() {{

                        newUrlButtonLine("「 投票地址 」", "https://t.me/" + TwitterSpam.VOTE_CHANNEL + "/" + v.vote_message_id);

                    }}).exec();

        }

        user.point = null;
        user.save();

        showList(user, msg, false, list.id);

    }

    public long[] parseIDs(List<String> list) {

        long[] ids = new long[list.size()];

        for (int index = 0;index < list.size();index ++) {

            ids[index] = Long.parseLong(list.get(index));

        }

        return ids;

    }

    public void addList(UserData user, Callback callback) {

        if (!user.isAdmin) {

            callback.alert("ERROR");

            return;

        }

        user.point = callback.data;
        user.point.setPoint(POINT_INPUT_LIST_CAUSE);
        user.save();

        callback.send("好，现在输入原因").exec();

    }

    public void onInputListCause(UserData user, Msg msg) {

        user.point.put("cause", msg.text());
        user.point.setPoint(POINT_ADD_LIST);
        user.save();

        msg.send("好，现在发送导出的 .csv文件").exec();

    }

    public void onInputCsv(UserData user, Msg msg) {

        if (msg.doc() == null) {

            msg.send("好像没有发送文件过来... >_<", "", "使用 /cancel 取消导入").exec();

            return;

        }

        if (!msg.doc().fileName().endsWith(".csv")) {

            msg.send("好像没有发送 **csv** 文件过来 （￣～￣）", "", "使用 /cancel 取消导入").exec();

            return;

        }

        try {

            Twitter api = user.twitterAccounts.getFirst().createApi();

            File csv = msg.file();

            List<String> lines = new LinkedList<String>(FileUtil.readUtf8Lines(csv));

            LinkedList<UserSpam> all = new LinkedList<>();

            SpamList list = msg.fragment.main.getSpamList(user.point.getIndex());

            String cause = user.point.getStr("cause");

            LinkedList<String> cache = new LinkedList<>();

            while (!lines.isEmpty()) {

                int target = 99;

                if (lines.size() < 100) {

                    target = lines.size() - 1;

                }

                ResponseList<User> users = api.lookupUsers(parseIDs(lines.subList(0, target)));

                for (User u : users) {

                    UserSpam spam = new UserSpam(list);

                    spam.twitterAccountId = u.getId();
                    spam.twitterDisplyName = u.getName();
                    spam.twitterScreenName = u.getScreenName();
                    spam.origin = user.id;
                    spam.spamCause = cause;

                    spam.put("list_id", list.id);

                    all.add(spam);

                    if (cache.size() < 20) {

                        cache.add(TApi.formatUserNameMarkdown(u) + "  [导入](https://t.me/NTToolsBot?start=" + Base64.encode(spam.save().toString()) + ")");

                    } else {

                        msg.send(cache.toArray(new String[cache.size()])).markdown().disableLinkPreview().exec();

                        cache.clear();

                    }



                }

                lines = lines.subList(target, lines.size() - 1);

            }

            if (cache.size() > 0) {

                msg.send(cache.toArray(new String[cache.size()])).markdown().disableLinkPreview().exec();

            }
            
            user.point = null;
            user.save();

            showList(user, msg, false, list.id);

        } catch (TwitterException e) {

            throw new RuntimeException(e);

        }

    }

    public void parsePayload(UserData user, Msg msg) {

        if (user.isAdmin) {

            try {

                JSONObject spamObj = new JSONObject(Base64.decode(msg.commandParms()[0]));

                SpamList list = msg.fragment.main.getSpamList(spamObj.getStr("list_id"));

                spamObj.remove("list_id");

                UserSpam spam = new UserSpam(list, spamObj);

                final String url = msg.fragment.main.spam.newSpam(list, spam);

                msg.send("添加成功 ~").buttons(new ButtonMarkup() {{

                            newUrlButtonLine("公开地址", url);

                        }}).exec();

            } catch (Exception exc) {}

        }


    }


    @Override
    public boolean processCallbackQuery(UserData user, Callback callback,boolean point) {

        if (point) return false;
        
        switch (callback.data.getPoint()) {

                case POINT_PUBLIC_LISTS : publicLists(user, callback, true);callback.confirm();break;
                case POINT_NEW_LIST : newList(user, callback);callback.confirm();break;
                case POINT_SHOW_LIST : showList(user, callback, true, callback.data.getIndex());callback.confirm();break;

                case POINT_SHOW_SPAM_USERS : showListSpams(user, callback);break;

                case POINT_NEW_SPAM : newSpamRequest(user, callback, false, false);break;
                case POINT_ADD_SPAM : newSpamRequest(user, callback, true, false);break;
                case POINT_REM_SPAM : newSpamRequest(user, callback, false, true);break;

                case POINT_CLEAR : clearList(user, callback);break;
                case POINT_ADD_LIST : addList(user, callback);break;

                case POINT_EDIT_LIST_NAME : editName(user, callback);break;
                case POINT_EDIT_LIST_DESC : editDesc(user, callback);break;

                case POINT_DELETE_LIST : deleteList(user, callback);break;

                case POINT_OPEN : enable(user, callback);break;
                case POINT_CLOSE : disable(user, callback);break;

                case POINT_BACK : sendMain(user, callback, true);callback.confirm();break;

                default : return false;

        }

        return true;

    }

}
