package io.kurumi.ntbot.ui;

import cn.hutool.log.*;
import com.pengrad.telegrambot.*;
import com.pengrad.telegrambot.model.*;
import io.kurumi.ntbot.*;
import java.util.*;

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

            ProcessIndex.processUpdate(update);

        }

        return CONFIRMED_UPDATES_ALL;

    }

}
