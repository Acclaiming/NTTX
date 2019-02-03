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

public class NTTBot extends MainFragment {

    public static final boolean debug = true;

    public NTTBot() {

        super(new File("./data"));

        fragments.add(TwitterUI.INSTANCE);
        fragments.add(SpamUI.INSTANCE);
        fragments.add(TwitterDataParser.INSTANCE);

    }

    @Override
    public boolean processPrivateMessage(UserData user, Msg msg) {

        if (debug && !user.isAdmin) {

            msg.send("对不起 但是BOT正在维护中 (Ｔ▽Ｔ)", "请稍后再来 (◦˙▽˙◦)").exec();

            return true;

        }

        if (msg.isCommand()) {

            switch (msg.commandName()) {

                    case "start" : case "help" : {

                        help(user, msg);

                        return true;

                    }
                    
                    case "admin" :
                        
                      admin(user,msg);
                      
                      return true;

            }

        }

        return false;

    }

    @Override
    public boolean processCallbackQuery(UserData user, Callback callback) {

        if (debug && !user.isAdmin) {

            callback.alert("对不起 但是BOT正在维护中 (Ｔ▽Ｔ)\n请稍后再来 (◦˙▽˙◦)");

            return true;

        } else return false;

    }
    
    public void admin(UserData user, Msg msg) {
        
        if (user.isAdmin && msg.commandParms().length == 2) {
            
            switch (msg.commandParms()[0]) {
                
                case "set" : {
                    
                    String targetStr =  (msg.commandParms()[1]);
                    
                    if (targetStr.startsWith("@"))  {
                        
                        UserData target = findUserData(targetStr.substring(1));

                        target.isAdmin = true;

                        target.save();

                        msg.send(target.name + " (@" + target.userName + ") 已被设为管理员 (◦˙▽˙◦)").send();

                        new Send(this,target.id,"您已被 " + user.name + " (@" + user.userName + ") 设为管理员 (◦˙▽˙◦)").exec();
                        
                    }

                    
                    
                }
                
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
