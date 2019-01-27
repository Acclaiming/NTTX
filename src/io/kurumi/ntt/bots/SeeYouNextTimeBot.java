package io.kurumi.ntt.bots;

import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.request.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.ui.confs.*;
import io.kurumi.ntt.ui.request.*;
import java.util.*;

public class SeeYouNextTimeBot extends TelegramUserBot {

    public static String NAME = "SeeYouNextTimeBot";
    public static String COMMAND = "see you next time";

    public static final String TYPE = "see_you_next_tume";

    public SeeYouNextTimeBot(UserData owner,String name) {
        super(owner,name);
    }

    @Override
    public String type() {
        
        return TYPE;
        
     }

    @Override
    public String[] allowUpdates() {
        return new String[] { UPDATE_TYPE_MESSAGE };
    }

    @Override
    public AbsResuest processUpdate(Update update) {

        switch (update.message().chat().type()) {

                case group : 
                case supergroup :

                return processGrpupMessage(update.message());

        }

        return null;

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
