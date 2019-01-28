package io.kurumi.ntt.ui.entries;

import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.ui.*;
import io.kurumi.ntt.ui.request.*;
import io.kurumi.ntt.webhookandauth.*;
import twitter4j.*;

public class Account {

    public static final String MAIN = "users|main";

    public static final String BACK_TO_USERLIST = "users|back";

    public static final String ADD_ACCOUNT = "users|add";

    public static final String MANAGE_ACCOUNT = "users|manage";

    public static final String DEL_ACCOUNT = "users|del";
    public static final String DEL_ALL_STATUS = "users|dels";
    
    public static final String CANCEL_DEL_ACCOUNT = "users|del|cancel";
    public static final String CONFIRM_DEL_ACCOUNT = "users|del|comfirm";

    public static final String POINT_INPUT_AUTH_URL = "users|input_auth_url";

    public static String[] userManageMsg = new String[] {

        "管理已认证的Twitter账号 ~",

    };

    public static AbsResuest onCallBack(final UserData userData, final DataObject obj) {

        switch (obj.getPoint()) {

                case MAIN : case BACK_TO_USERLIST : 
                return main(userData, obj,true);

                case ADD_ACCOUNT :
                    
                return addAccount(userData, obj);

                case DEL_ACCOUNT :
                return confirmDelete(userData, obj);

                case MANAGE_ACCOUNT : 
                return manageAccount(userData, obj);

                case CONFIRM_DEL_ACCOUNT :
                case CANCEL_DEL_ACCOUNT :
                return onAccountDel(userData, obj);
                
                
                case DEL_ALL_STATUS :
                    
                new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try {

                                delAllStatus(userData, obj);
                            } catch (TwitterException e) {

                                obj.send("api limit... ").exec();

                            }
                        }
                    }).start(); 
                    
                
                
                return null;

        }
        
        return obj.reply().alert("非法的用户管理指针 : " + obj.getPoint());

    }

    private static AbsResuest delAllStatus(UserData userData, DataObject obj) throws TwitterException {
        
        obj.reply().text("正在开始").exec();
        
        Twitter api =  obj.getUser(userData).createApi();
        
        ResponseList<Status> tl = api.getUserTimeline(new Paging().count(200));

        while (tl.size() != 0) {
            
            for(Status s : tl) {
                
                api.destroyStatus(s.getId());
                
                String text = s.getText();
                
           
                obj.send("已删除 : " + text).exec();
                
            }
            
            tl = api.getUserTimeline(new Paging().count(200));
            
        }
        
        obj.send("删除完成 (￣▽￣)~*").exec();
        
        return null;
    }

    public static AbsResuest main(final UserData userData, DataObject obj,boolean edit) {

        AbsSendMsg send;

        if (!edit) {

            send = new SendMsg(obj.chat(), userManageMsg);

        } else {
            
            send =  new EditMsg(obj.msg(), userManageMsg);
        }

        send.singleLineButton("添加新账号 (●'◡'●)", ADD_ACCOUNT);

        for (TwiAccount account : userData.twitterAccounts) {

            send.singleLineButton("管理 @" + account.screenName, MANAGE_ACCOUNT, account);

        }

        send.singleLineButton("<< 返回 ପ( ˘ᵕ˘ ) ੭ ☆", MainUI.BACK_TO_MAIN);

        return send;

    }

    public static AbsResuest addAccount(final UserData userData, final DataObject obj) {

        final String authUrl = Constants.authandwebhook.newRequest(new AuthListener() {

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

                    main(userData, obj,false).exec();

                }

            });
            
        startAuth(userData, obj, authUrl);

        if (authUrl == null) {

            return obj.reply().alert("请求认证失败... 请稍后再试");

        } else {

            return obj.reply().text("请求认证成功 ~");

        }

        
    }

    public static void startAuth(UserData userData, DataObject obj, final String authUrl) {

        String[] authMsg;

        if (!Constants.data.useServer) {

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


    public static AbsResuest onInputUrl(UserData userData, Message msg) {

        TwiAccount account = Constants.authandwebhook.authByUrl(msg.text());

        if (account == null) {

            return new SendMsg(msg.chat(), "链接无效 ( •̥́ ˍ •̀ू ) 请重新发送\n或者使用 /cancel 以取消认证..");

        }

        userData.point = null;
        userData.save();

        return null;
        
    }

    public static AbsResuest manageAccount(UserData userData, DataObject obj) {

        final TwiAccount account = obj.getUser(userData);

        return new EditMsg(obj.msg(), "编辑账号 : " + account.getFormatedName()) {{

                singleLineButton("删除 y( ˙ᴗ. )~", DEL_ACCOUNT, account);

                singleLineOpenUrlButton("打开主页 ପ( ˘ᵕ˘ ) ੭ ☆", account.getUrl());

                singleLineButton("删除所有推文 (慎用)", DEL_ALL_STATUS, account);
                
                
                singleLineButton("<< 返回账号列表", BACK_TO_USERLIST);

            }};

    }

    public static AbsResuest confirmDelete(UserData userData, DataObject obj) {

        final TwiAccount account = obj.getUser(userData);
        
        return new EditMsg(obj.msg(), "真的要删除账号 : " + account.getFormatedName() + " 吗？") {{

                singleLineButton("是点错了 ！ 请不要删掉这个账号 （ｉДｉ）", CANCEL_DEL_ACCOUNT, account);

                singleLineButton("是的，删掉这个账号吧 ！ (￣▽￣)~*", CONFIRM_DEL_ACCOUNT, account);

                singleLineButton("不要删掉啦！ (。・`ω´・)", CANCEL_DEL_ACCOUNT, account);

                singleLineButton("<< 返回 继续管理这个账号 (ﾉ｀⊿´)ﾉ", MANAGE_ACCOUNT , account);

            }};


    }

    public static AbsResuest onAccountDel(final UserData userData, DataObject obj) {

        if (CONFIRM_DEL_ACCOUNT.equals(obj.getPoint())) {

            userData.twitterAccounts.remove(obj.getUser(userData));

            userData.save();
            
            main(userData, obj,false).exec();
            
            return obj.reply().text("已删除 ~");

        } else {
            
            manageAccount(userData, obj).exec();
           
            return obj.reply().text("已取消 ~");

            
        }

    }


}
