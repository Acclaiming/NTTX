package io.kurumi.ntt.fragment;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import io.kurumi.ntt.BotConf;
import io.kurumi.nttools.fragments.FragmentBase;
import java.util.LinkedList;
import java.util.List;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.DeleteWebhook;

public abstract class BotFragment extends Fragment implements UpdatesListener {

    public LinkedList<FragmentBase> fragments = new LinkedList<>();

    public abstract String botName();

    public boolean isLongPulling() { return false; }

    public void start() {

        String token = BotConf.getBotToken(botName());

        if (token == null || !BotConf.verifyToken(token)) {
            
            token = BotConf.inputToken(botName());
            
        }
        
        bot = new TelegramBot.Builder(token).build();
        
        if (isLongPulling()) {
            
            bot.setUpdatesListener(this,new GetUpdates());
            
        } else {
            
            
        }
        
    }
    
    public void stop() {
        
        if (isLongPulling()) {
            
            bot.removeGetUpdatesListener();
            
        } else {
            
            bot.execute(new DeleteWebhook());
            
        }
        
    }

    @Override
    public int process(List<Update> update) {
        
        return CONFIRMED_UPDATES_ALL;
        
    }

}
