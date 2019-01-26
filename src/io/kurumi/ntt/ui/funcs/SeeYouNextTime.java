package io.kurumi.ntt.ui.funcs;

import io.kurumi.ntt.*;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.*;
import com.pengrad.telegrambot.*;
import java.io.*;
import io.kurumi.ntt.ui.request.*;

public class SeeYouNextTime {

    public static String COMMAND = "see you next time";

    public static void processGrpupMessage(UserData userData, final Message msg) {

        if (msg.text() == null) return;
        
        if (msg.text().contains(COMMAND)) {

            RestrictChatMember restrict = new RestrictChatMember(msg.chat().id(), msg.from().id())
                .untilDate(1)
                .canSendMessages(false)
                .canSendMediaMessages(false)
                .canSendOtherMessages(false)
                .canAddWebPagePreviews(false);

            Constants.bot.execute(restrict,new Callback<RestrictChatMember,BaseResponse>() {

                    @Override
                    public void onResponse(RestrictChatMember request, BaseResponse resp) {
                        
                        if (resp.isOk()) {
                            
                            new SendMsg(msg,"~").exec();
                            
                        }
                        
                    }

                    @Override
                    public void onFailure(RestrictChatMember p1, IOException p2) {
                    }
                    
                });

        }

    }

}
