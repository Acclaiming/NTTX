package io.kurumi.ntt.twitter;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.BotConf;
import io.kurumi.ntt.model.request.ButtonMarkup;
import io.kurumi.ntt.db.WrongUse;
import java.util.LinkedList;

public class TwitterUI extends Fragment {

    public static TwitterUI INSTANCE = new TwitterUI();
    
    @Override
    public boolean onMsg(UserData user, Msg msg) {

        if (!msg.isCommand()) return false;

        switch (msg.commandName()) {

                case "login" : login(user, msg); break;
                case "su" : su(user, msg); break;
                case "logout" : logout(user, msg); break;

                default : return false;

        }

        return true;

    }

    public void login(final UserData user, final Msg msg) {

        TwiAuthF.pre(user, new TwiAuthF.Listener() {

                @Override
                public void onAuth(String oauthVerifier) {
                }

                @Override
                public void onAuth(TwiAccount account) {

                    String n = account.formatedNameMarkdown() + " 认证成功 ⊙∀⊙";

                    if (!msg.isPrivate()) n = user.userName() + " " + n;

                    msg.send(n).exec();

                }

            });

        String text = "点击专属链接认证 ⊙∀⊙";

        if (!msg.isPrivate()) {

            text =  user.userName() + " " + text;

        }

        msg.send(text).buttons(new ButtonMarkup() {{

                    newUrlButtonLine("认证", "https://" + BotConf.SERVER_DOMAIN + "/auth?userId=" + user.id);

                }}).exec();

    }


    public void su(final UserData user, Msg msg) {

        if (msg.commandParms().length != 1) {

            msg.send(user.userName() + " " + WrongUse.incrWithMsg(user), "请使用 /su 用户名 切换账号").exec();

            return;

        }

       
            TwiAccount acc = TwiAccount.getByScreenName(msg.commandParms()[0]);

            if (acc == null && user.isBureaucrats()) {

                msg.send("这个账号没有被认证过 （￣～￣）").exec();

                return;

            } else if (!user.id.equals(acc.id) && !user.isBureaucrats()) {

                msg.send("你认证这个账号了吗？ ", "", WrongUse.incrWithMsg(user)).exec();

                return;

            }

            TwiAccount.switchAccount(user.id, acc);
    }

    public void logout(UserData user, Msg msg) {

        if (user.current() == null) {

            msg.send("你还没有登录账号！", "", WrongUse.incrWithMsg(user)).exec();
            
            return;

        }
        
        TwiAccount current = user.current();

        if (current.belong.equals(user.id)) {
            
            current.logout();
            
            msg.send("移除 " + current.formatedNameMarkdown() + " 完成 （￣～￣）").markdown().disableLinkPreview().exec();
            
        } else {
            
            LinkedList<TwiAccount> accounts = TwiAccount.getAccounts(user.id);
            
            if (accounts.isEmpty()) {
                
                TwiAccount.cleanCurr(user.id);
                
                msg.send("切出 " + current.formatedNameMarkdown() + " 完成","","当然没有登录的账号 >_<").markdown().disableLinkPreview().exec();
                
            } else {
                
                TwiAccount.switchAccount(user.id,accounts.getFirst());
                
                msg.send("切回 " + accounts.getFirst().formatedNameMarkdown() + " 完成 （￣～￣）").markdown().disableLinkPreview().exec();
                
            }

        }
        
        
    }

}
