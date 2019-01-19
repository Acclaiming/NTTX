package io.kurumi.ntbot.ui;

import cn.hutool.log.*;
import com.pengrad.telegrambot.*;
import com.pengrad.telegrambot.model.*;
import io.kurumi.ntbot.*;
import java.util.*;
import com.pengrad.telegrambot.request.*;
import io.kurumi.ntbot.ui.callback.*;

public class MainAdapter implements UpdatesListener {

    public Log log;
    
    public BotMain bot;
    public Data data;
    
    public MainAdapter(BotMain bot) {
        this.bot = bot;
        data = bot.data;
        log = StaticLog.get("MainAdapter");
    }

    @Override
    public int process(List<Update> updates) {

        for (Update update : updates) {

            processUpdate(update);

        }

        return CONFIRMED_UPDATES_ALL;

    }

    public void processUpdate(Update update) {
        
      //  log.debug("处理更新 : " + update.toString());
        
      try {
     
        processMseeage(update.message());
        processCallBack(update.callbackQuery());
        
        } catch(Exception e) {e.printStackTrace(); }

    }

    public void processCallBack(CallbackQuery callbackQuery) {
        
        InlineCallback.process(callbackQuery);
        
    }

    public void processMseeage(Message msg) {

        if (msg == null) return;
        
        log.debug("处理消息 : " + msg.text());

        switch (msg.chat().type()) {

            case Private : processPrivateMassage(msg);break;
            case group  : break;
            case supergroup : break;
            case channel : break;

        }

    }

    public void processPrivateMassage(Message msg) {

        UserData userData = data.getUser(msg.from());

        userData.getInterface().processMessage(msg);

    }

}
