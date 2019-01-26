package io.kurumi.ntt.ui.entries;

import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.auth.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.ui.*;
import io.kurumi.ntt.ui.ext.*;
import io.kurumi.ntt.md.*;
import io.kurumi.ntt.ui.ext.MsgExt.*;

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
                confirmDelete(userData,obj);
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
                changeTo(userData,obj);
                obj.confirmQuery();
                return;

        }

    }

    public static void changeTo(final UserData userData, DataObject obj) {

        if (obj == null) {
            
            new MsgExt.Send(userData.chat,userManageMsg) {{

                    inlineCallbackButton("添加账号", ADD_ACCOUNT);

                    for (TwiAccount account : userData.twitterAccounts) {

                        DataObject manageUserObj = new DataObject();

                        manageUserObj.setPoint(MANAGE_ACCOUNT);

                        manageUserObj.put("accountId", account.accountId);

                        inlineCallbackButton("@" + account.screenName, manageUserObj);

                    }

                    inlineCallbackButton("<< 返回主页", MainUI.BACK_TO_MAIN);

                }}.send();
                
                return;
            
        }
        
        new MsgExt.Edit(obj.msg(), userManageMsg) {{

                inlineCallbackButton("添加新的Twitter账号 (●'◡'●)", ADD_ACCOUNT);

                for (TwiAccount account : userData.twitterAccounts) {

                    DataObject manageUserObj = new DataObject();

                    manageUserObj.setPoint(MANAGE_ACCOUNT);

                    manageUserObj.put("accountId", account.accountId);

                    inlineCallbackButton("管理 @" + account.screenName, manageUserObj);

                }

                inlineCallbackButton("<< 返回主页", MainUI.BACK_TO_MAIN);

            }}.edit();

    }

    public static void addAccount(final UserData userData, final DataObject obj) {

        final String authUrl = Constants.auth.newRequest(new AuthListener() {

                @Override
                public void onAuth(TwiAccount account) {

                    obj.deleteMsg();
                    
                    if (userData.twitterAccounts.contains(account)) {

                        obj.send(account.getFormatedName() + " 更新成功 ~").send();

                        userData.twitterAccounts.remove(account);


                    } else {

                        obj.send(account.getFormatedName() + " 认证成功 ~").send();

                    }

                    userData.twitterAccounts.add(account);

                    userData.save();

                    changeTo(userData, null);

                }

            });

        if (authUrl == null) {

            obj.reply().alert("请求认证失败... 请稍后再试").reply();

        } else {

            obj.reply().text("请求认证成功 ~").reply();

        }

        startAuth(userData, obj, authUrl);

    }

    public static void startAuth(UserData userData, DataObject obj, final String authUrl) {

        String[] authMsg;

        if (!Constants.data.useAuthServer) {

            userData.point = POINT_INPUT_AUTH_URL;
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

        new MsgExt.Edit(obj.msg() , authMsg) {{

                inlineOpenUrlButton("认证", authUrl);

                inlineCallbackButton("退出", BACK_TO_USERLIST);

            }}.edit();

    }


    public static void onInputUrl(UserData userData, Message msg) {

        TwiAccount account = Constants.auth.authByUrl(msg.text());

        if (account == null) {

            new MsgExt.Send(msg.chat(), "链接无效 ( •̥́ ˍ •̀ू ) 请重新发送\n或者使用 /cancel 以取消认证..").send();
            return;

        }

        userData.point = "";
        userData.save();

    }

    public static void manageAccount(UserData userData, DataObject obj) {

        final TwiAccount account = userData.find(obj.getLong("accountId"));

        new MsgExt.Edit(obj.msg(), "编辑账号 : " + account.getFormatedName()) {{

                DataObject delAccountObj = new DataObject();

                delAccountObj.setPoint(DEL_ACCOUNT);

                delAccountObj.put("accountId", account.accountId);

                inlineCallbackButton("删除这个账号 y( ˙ᴗ. )~", delAccountObj);

                inlineOpenUrlButton("打开主页 ପ( ˘ᵕ˘ ) ੭ ☆", account.getUrl());

                inlineCallbackButton("<< 返回账号列表", BACK_TO_USERLIST);

            }}.edit();

    }

    public static void confirmDelete(UserData userData, DataObject obj) {

        final TwiAccount account = userData.find(obj.getLong("accountId"));

        new MsgExt.Edit(obj.msg(), "真的要删除账号 : " + account.getFormatedName() + " 吗？") {{

                DataObject confirmDelAccountObj = new DataObject();

                confirmDelAccountObj.setPoint(CONFIRM_DEL_ACCOUNT);

                confirmDelAccountObj.put("accountId", account.accountId);

                DataObject cancelDelAccountObj = new DataObject();

                cancelDelAccountObj.setPoint(CANCEL_DEL_ACCOUNT);

                cancelDelAccountObj.put("accountId", account.accountId);

                inlineCallbackButton("是点错了 ！ 不要删掉 （ｉДｉ）", cancelDelAccountObj);

                inlineCallbackButton("是的，删掉吧 ！ (￣▽￣)~*", confirmDelAccountObj);

                inlineCallbackButton("是手贱了！", cancelDelAccountObj);

                inlineCallbackButton("<< 返回账号", BACK_TO_USERLIST);

            }}.edit();


    }

    public static void onAccountDel(final UserData userData, DataObject obj) {

        if (CONFIRM_DEL_ACCOUNT.equals(obj.getPoint())) {

            userData.twitterAccounts.remove(userData.find(obj.getLong("accountId")));

            obj.reply().text("已删除 ~").reply();

            changeTo(userData, obj);

        } else {

            obj.reply().text("已取消 ~").reply();

            manageAccount(userData, obj);

        }

    }


}
