package io.kurumi.ntt.ui.entries;

import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.auth.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.ui.*;
import io.kurumi.ntt.ui.ext.*;
import io.kurumi.ntt.md.*;
import io.kurumi.ntt.ui.ext.MsgExt.*;
import io.kurumi.ntt.ui.request.*;

public class Account {

    public static final String BACK_TO_USERLIST = "users|back";

    public static final String ADD_ACCOUNT = "users|add";

    public static final String MANAGE_ACCOUNT = "users|manage";

    public static final String DEL_ACCOUNT = "users|del";
    public static final String CANCEL_DEL_ACCOUNT = "users|del|cancel";
    public static final String CONFIRM_DEL_ACCOUNT = "users|del|comfirm";

    public static final String POINT_INPUT_AUTH_URL = "users|input_auth_url";

    public static String[] userManageMsg = new String[] {

        "管理已认证的Twitter账号 ~",

    };

    public static void onCallBack(UserData userData, DataObject obj) {

        switch (obj.getPoint()) {

                case ADD_ACCOUNT :

                addAccount(userData, obj);
                obj.confirmQuery();
                return;

                case DEL_ACCOUNT :
                confirmDelete(userData, obj);
                obj.confirmQuery();
                return;

                case MANAGE_ACCOUNT : 
                manageAccount(userData, obj);
                obj.confirmQuery();
                return;

                case CONFIRM_DEL_ACCOUNT :
                case CANCEL_DEL_ACCOUNT :
                onAccountDel(userData, obj);
                return;

                case BACK_TO_USERLIST :
                changeTo(userData, obj);
                obj.confirmQuery();
                return;

        }

    }

    public static void changeTo(final UserData userData, DataObject obj) {

        AbsSendMsg send;

        if (obj == null) {

            send = new SendMsg(userData.chat, userManageMsg);

        } else {
            send =  new EditMsg(obj.msg(), userManageMsg);
        }
        send.singleLineButton("添加新账号 (●'◡'●)", ADD_ACCOUNT);

        for (TwiAccount account : userData.twitterAccounts) {

            send.singleLineButton("管理 @" + account.screenName, MANAGE_ACCOUNT, account);

        }

        send.singleLineButton("<< 返回 ପ( ˘ᵕ˘ ) ੭ ☆", MainUI.BACK_TO_MAIN);

        send.exec();

    }

    public static void addAccount(final UserData userData, final DataObject obj) {

        final String authUrl = Constants.auth.newRequest(new AuthListener() {

                @Override
                public void onAuth(TwiAccount account) {

                    obj.deleteMsg();

                    if (userData.twitterAccounts.contains(account)) {

                        obj.send(account.getFormatedName() + " 更新成功 ~").exec();

                        userData.twitterAccounts.remove(account);


                    } else {

                        obj.send(account.getFormatedName() + " 认证成功 ~").exec();

                    }

                    userData.twitterAccounts.add(account);

                    userData.save();

                    changeTo(userData, null);

                }

            });

        if (authUrl == null) {

            obj.reply().alert("请求认证失败... 请稍后再试").exec();

        } else {

            obj.reply().text("请求认证成功 ~").exec();

        }

        startAuth(userData, obj, authUrl);

    }

    public static void startAuth(UserData userData, DataObject obj, final String authUrl) {

        String[] authMsg;

        if (!Constants.data.useAuthServer) {

            userData.setPoint(POINT_INPUT_AUTH_URL);
            userData.save();

            authMsg = new String[] {

                "点击 「认证」 来登录哦！",
                "认证之后会跳转到一个本地 (127.0.0.1) 网页",
                "复制链接发给咱就行了呢...","",
                "注意 : 只能认证一次哦.. 注意复制链接地址 〒▽〒",
                "取消认证用 /cancel 和 「取消」 哦 ！ "

            };

        } else {

            authMsg = new String[] {

                "点击 「认证」 按钮来登录哦！",
                "取消登录用 「取消」 ~",

            };

        }

        new EditMsg(obj.msg() , authMsg) {{

                newInlineButtonGroup()
                    .newOpenUrlButton("认证", authUrl)
                    .newButton("退出", BACK_TO_USERLIST);

            }}.exec();

    }


    public static void onInputUrl(UserData userData, Message msg) {

        TwiAccount account = Constants.auth.authByUrl(msg.text());

        if (account == null) {

            new SendMsg(msg.chat(), "链接无效 ( •̥́ ˍ •̀ू ) 请重新发送\n或者使用 /cancel 以取消认证..").exec();
            return;

        }

        userData.point = null;
        userData.save();

    }

    public static void manageAccount(UserData userData, DataObject obj) {

        final TwiAccount account = obj.getUser(userData);

        new EditMsg(obj.msg(), "编辑账号 : " + account.getFormatedName()) {{

                singleLineButton("删除 y( ˙ᴗ. )~", DEL_ACCOUNT, account);

                singleLineButton("打开主页 ପ( ˘ᵕ˘ ) ੭ ☆", account.getUrl());

                singleLineButton("<< 返回账号列表", BACK_TO_USERLIST);

            }}.exec();

    }

    public static void confirmDelete(UserData userData, DataObject obj) {

        final TwiAccount account = obj.getUser(userData);

        new EditMsg(obj.msg(), "真的要删除账号 : " + account.getFormatedName() + " 吗？") {{

                singleLineButton("是点错了 ！ 请不要删掉这个账号 （ｉДｉ）", CANCEL_DEL_ACCOUNT, account);

                singleLineButton("是的，删掉这个账号吧 ！ (￣▽￣)~*", CONFIRM_DEL_ACCOUNT, account);

                singleLineButton("不要删掉啦！ (。・`ω´・)", CANCEL_DEL_ACCOUNT, account);

                singleLineButton("<< 返回 继续管理这个账号 (ﾉ｀⊿´)ﾉ", MANAGE_ACCOUNT , account);

            }}.exec();


    }

    public static void onAccountDel(final UserData userData, DataObject obj) {

        if (CONFIRM_DEL_ACCOUNT.equals(obj.getPoint())) {

            userData.twitterAccounts.remove(obj.getUser(userData));

            obj.reply().text("已删除 ~").exec();

            changeTo(userData, obj);

        } else {

            obj.reply().text("已取消 ~").exec();

            manageAccount(userData, obj);

        }

    }


}
