package io.kurumi.nttools.spam;

import io.kurumi.nttools.fragments.FragmentBase;
import io.kurumi.nttools.utils.UserData;
import io.kurumi.nttools.model.Msg;
import io.kurumi.nttools.model.request.ButtonMarkup;
import io.kurumi.nttools.model.request.ButtonLine;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;

public class SpamUI extends FragmentBase {

    public static final SpamUI INSTANCE = new SpamUI();

    public static final String ALL_TYPE_LIST = "s|at";

    @Override
    public boolean processPrivateMessage(UserData user, Msg msg) {

        if (!msg.isCommand() || msg.commandName().equals("spam")) return false;

        sendMain(user, msg, false);

        return true;

    }

    private void sendMain(final UserData user, Msg msg, boolean edit) {

        String[] spamMsg = new String[] {

            "「Twitter联合封禁」目录 :",

        };

        BaseResponse resp = sendOrEdit(msg, edit, spamMsg)

            .buttons(new ButtonMarkup() {{

                    newButtonLine("所有分类", ALL_TYPE_LIST);

                }}).exec();
                
        if (resp instanceof SendResponse) {
            
            ((SendResponse)resp).message().messageId();
            
        }

    }

}
