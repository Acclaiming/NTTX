package io.kurumi.ntt.ui.funcs;

import io.kurumi.ntt.*;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.*;
import com.pengrad.telegrambot.*;
import java.io.*;
import io.kurumi.ntt.ui.request.*;

public class SeeYouNextTime extends UserBot {

    public static String COMMAND = "see you next time";

    public SeeYouNextTime(UserData owner,String token) {
        super(owner,token);
    }

    @Override
    public String[] allowUpdates() {
        return new String[] { UPDATE_TYPE_MESSAGE };
    }

    @Override
    public AbsResuest processUpdate(Update update) {


        return processGrpupMessage(update.message());

    }

    public AbsResuest processGrpupMessage(final Message msg) {

        if (msg.text() == null) return null;

        if (msg.text().toLowerCase().trim().contains(COMMAND)) {

            return new Pack<RestrictChatMember>
            (new RestrictChatMember(msg.chat().id(), msg.from().id())
             .untilDate(1)
             .canSendMessages(false)
             .canSendMediaMessages(false)
             .canSendOtherMessages(false)
             .canAddWebPagePreviews(false));

        }
        
        return null;

    }

}
