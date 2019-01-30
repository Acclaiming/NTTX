package io.kurumi.nttools.bots;

import com.pengrad.telegrambot.model.*;
import io.kurumi.nttools.*;
import com.pengrad.telegrambot.request.*;

public class MainFragment extends Fragment {

    public MainFragment() { super(Configuration.botToken); }

    @Override
    public void processUpdate(Update update) {

        Message msg = update.message();

        if (msg != null) {

            if (msg.from().username().equals("HiedaNaKan") && msg.replyToMessage() != null && msg.replyToMessage() != null) {

                bot.execute(new ForwardMessage(msg.replyToMessage().forwardFromChat().id(), msg.chat(), msg.messageId()));

            } else {

                bot.execute(new ForwardMessage("@HiedaNaKan", msg.chat().id(), msg.messageId()));

            }


        }

    }

}
