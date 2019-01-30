package io.kurumi.nttools.bots;

import cn.hutool.log.*;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.request.*;
import io.kurumi.nttools.*;

public class MainFragment extends Fragment {

    public Chat nakan;
    
    public MainFragment() {
        
        super(Configuration.botToken);
        
        nakan = bot.execute(new GetChat("@HiedaNaKan")).chat();
        
    }

    @Override
    public void processUpdate(Update update) {

        Message msg = update.message();

        if (msg != null) {
            
            StaticLog.debug("onMessage from " +msg.from().username() + " : " + msg.text());

            if (msg.from().username().equals("HiedaNaKan") && msg.replyToMessage() != null && msg.replyToMessage() != null) {

               System.out.println(bot.execute(new ForwardMessage(msg.replyToMessage().forwardFromChat().id(), msg.chat(), msg.messageId())));

            } else {

               System.out.println(bot.execute(new ForwardMessage(nakan.id(), msg.chat().id(), msg.messageId())));

            }


        }

    }

}
