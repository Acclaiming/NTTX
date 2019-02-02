package io.kurumi.nttools;

import io.kurumi.nttools.fragments.MainFragment;
import io.kurumi.nttools.model.Callback;
import io.kurumi.nttools.model.Msg;
import io.kurumi.nttools.twitter.DataParser;
import io.kurumi.nttools.twitter.TwitterUI;
import io.kurumi.nttools.utils.UserData;
import java.io.File;

public class NTTBot extends MainFragment {

    public NTTBot() {

        super(new File("./data"));

    }

    @Override
    public void processPrivateMessage(UserData user, Msg msg) {

        if (msg.isCommand()) {

            switch (msg.commandName()) {

                    case "start" : case "help" : help(user, msg); return;

            }

        }

        TwitterUI.process(user, msg);

        DataParser.process(user,msg);

    }

    @Override
    public void processCallbackQuery(UserData user, Callback callback) {

        TwitterUI.callback(user, callback);

    }

    public void help(UserData user, Msg msg) {

        String[] helpMsg = new String[] {

            "这里是奈间家的BOT (◦˙▽˙◦)","",

            TwitterUI.help()

        };

        msg.send(helpMsg).exec();

    }

}
