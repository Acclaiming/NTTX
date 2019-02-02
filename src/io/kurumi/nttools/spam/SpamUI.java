package io.kurumi.nttools.spam;

import io.kurumi.nttools.fragments.FragmentBase;
import io.kurumi.nttools.utils.UserData;
import io.kurumi.nttools.model.Msg;
import io.kurumi.nttools.model.request.ButtonMarkup;

public class SpamUI extends FragmentBase {

    public static final SpamUI INSTANCE = new SpamUI();

    public static final String ALL_TYPE_LIST = "s|at";
    
    @Override
    public void processPrivateMessage(UserData user, Msg msg) {

        if (!msg.isCommand() || msg.commandName().equals("spam")) return;

        sendMain(user, msg, false);

    }

    private void sendMain(UserData user, Msg msg, boolean edit) {

        String[] spamMsg = new String[] {

            "「Twitter联合封禁」目录 :. ",

        };

        sendOrEdit(msg, edit, spamMsg)
        
            .buttons(new ButtonMarkup() {{

                newUrlButtonLine("所有分类",ALL_TYPE_LIST);

                }});

    }

}
