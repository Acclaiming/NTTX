package io.kurumi.ntt.ui.funcs;

import io.kurumi.ntt.*;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.request.*;

public class SeeYouNextTime {

    public static String COMMAND = "see you next time";

    public static void processGrpupMessage(UserData userData, Message msg) {

        if (COMMAND.equals(msg.text())) {

            RestrictChatMember restrict = new RestrictChatMember(msg.chat().id(), msg.from().id())
                .untilDate(1)
                .canSendMessages(false)
                .canSendMediaMessages(false)
                .canSendOtherMessages(false)
                .canAddWebPagePreviews(false);


            Constants.bot.execute(restrict);

        }

    }

}
