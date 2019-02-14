package io.kurumi.nttools;

import io.kurumi.nttools.fragments.MainFragment;
import io.kurumi.nttools.model.Callback;
import io.kurumi.nttools.model.Msg;
import io.kurumi.nttools.twitter.TwitterDataParser;
import io.kurumi.nttools.twitter.TwitterUI;
import io.kurumi.nttools.utils.UserData;
import java.io.File;
import io.kurumi.nttools.spam.SpamUI;
import io.kurumi.nttools.model.request.Send;
import io.kurumi.nttools.spam.VoteUI;
import io.kurumi.nttools.twitter.Functions;
import com.pengrad.telegrambot.request.ForwardMessage;
import io.kurumi.nttools.timer.RefreshTwitterAccountTask;
import io.kurumi.nttools.timer.BackupTask;
import io.kurumi.nttools.fragments.Fragment;
import io.kurumi.nttools.fragments.FragmentBase;
import io.kurumi.nttools.raffle.SimpleRaffle;

public class NTTBot extends MainFragment {

    public boolean debug = false;
    public String debugMsg = "bot 正在紧急维护 （￣～￣）";

    public NTTBot(File dataDir) {

        super(dataDir);

        fragments.add(TwitterUI.INSTANCE);
        fragments.add(SpamUI.INSTANCE);
        fragments.add(VoteUI.INSTANCE);
        fragments.add(TwitterDataParser.INSTANCE);

        timer.tasks.add(BackupTask.INSTANCE);
        
        timer.tasks.add(VoteUI.INSTANCE);
        timer.tasks.add(RefreshTwitterAccountTask.INSTANCE);

        fragments.add(Functions.INSTANCE);
        
        fragments.add(SimpleRaffle.INSTANCE);

        
    }

    @Override
    public boolean processPrivateMessage(UserData user, Msg msg, boolean point) {

        if (point) return false;

        if (!user.isAdmin && debug) {

            msg.send(debugMsg).exec();
            return true;

        }


        if (msg.isCommand()) {

            switch (msg.commandName()) {

                    case "start" : 

                    if (msg.commandParms().length != 0) {

                        return false;

                    } else help(user, msg); break;

                    case "help" : 

                    help(user, msg);

                    break;

                    case "admin" :
                        
                    admin(user, msg);

                    break;
                    
                    case "ban" :

                    ban(user, msg);

                    break;

                    case "fd" :

                    fd(user, msg);

                    break;

                    case "debug" :

                    debug(user, msg);

                    break;

                    default : return false;


            }

        }

        return true;

    }

    public void debug(UserData user, Msg msg) {

        if (!user.isAdmin) return;

        user.save();

        msg.send(user.toStringPretty()).exec();

    }

    @Override
    public boolean processCallbackQuery(UserData user, Callback callback, boolean point) {

        if (!user.isAdmin && debug) {

            callback.alert(debugMsg);

            return true;

        } else return false;

    }

    public void fd(UserData user, Msg msg) {

        if (msg.message().replyToMessage() != null && user.isAdmin) {

            if ("q".equals(msg.commandParms()[0])) {

                for (UserData u : msg.fragment.main.getUsers()) {

                    msg.fragment.bot.execute(new ForwardMessage(u.id, msg.message().replyToMessage().chat().id(), msg.message().replyToMessage().messageId()).disableNotification(true));

                }

            } else if ("qx".equals(msg.commandParms()[0])) {

                for (UserData u : msg.fragment.main.getUsers()) {

                    if (!u.twitterAccounts.isEmpty()) {

                        msg.fragment.bot.execute(new ForwardMessage(u.id, msg.message().replyToMessage().chat().id(), msg.message().replyToMessage().messageId()).disableNotification(true));

                    }

                }

            } else if ("x".equals(msg.commandParms()[0])) {

                for (UserData u : msg.fragment.main.getUsers()) {

                    if (!u.twitterAccounts.isEmpty()) {

                        msg.fragment.bot.execute(new ForwardMessage(u.id, msg.message().replyToMessage().chat().id(), msg.message().replyToMessage().messageId()));

                    }

                }

            } else {


                for (UserData u : msg.fragment.main.getUsers()) {
                    
                    msg.fragment.bot.execute(new ForwardMessage(u.id, msg.message().replyToMessage().chat().id(), msg.message().replyToMessage().messageId()));


                }



            }



        }

    }


    public void admin(UserData user, Msg msg) {

        if ("HiedaNaKan".equals(user.userName) && msg.commandParms().length == 2) {

            String targetStr =  msg.commandParms()[0];

            if (targetStr.startsWith("@"))  {

                targetStr = targetStr.substring(1);

            }

            UserData target = findUserData(targetStr);

            boolean action = Boolean.parseBoolean(msg.commandParms()[1]);

            if (target != null) {

                target.isAdmin = action;

                target.save();

                msg.send(target.name + " (@" + target.userName + ") 已被设置了管理员权限 : " + action).send();

                if (action) {

                    new Send(this, target.id, "您已被 " + user.name + " (@" + user.userName + ") 设为管理员 (◦˙▽˙◦)").exec();

                }

            } else {

                msg.send("本地没有此用户的数据 : @" + targetStr).exec();

            }

        }

    }
    
    public void ban(UserData user, Msg msg) {

        if (user.isAdmin && msg.commandParms().length == 2) {

            String targetStr =  msg.commandParms()[0];

            if (targetStr.startsWith("@"))  {

                targetStr = targetStr.substring(1);

            }

            UserData target = findUserData(targetStr);
            
            boolean action = Boolean.parseBoolean(msg.commandParms()[1]);
            
            
            if (target.isAdmin) {
                
                msg.send("对方也是滥权管理员 请送 @HiedaNaKan 处理").exec();
                
            //    new Send(this,user.markdownName() + " 请求设置封禁 " + target.markdownName() + " ")
                
                return;
                
            }

            
            if (target != null) {

                target.isBanned = action;

                target.save();

                msg.send(target.name + " (@" + target.userName + ") 已被设置封禁 : " + action).send();

            } else {

                msg.send("本地没有此用户的数据 : @" + targetStr).exec();

            }

        }

    }

    public void help(UserData user, Msg msg) {

        String[] helpMsg = new String[] {

            "这里是奈间家的BOT (◦˙▽˙◦)","",

            TwitterUI.help,
            SpamUI.help,

            "",
            "",

            TwitterDataParser.help


        };

        msg.send(helpMsg).exec();

    }

}
