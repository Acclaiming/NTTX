package io.kurumi.ntt.ui;

import cn.hutool.log.*;
import com.pengrad.telegrambot.*;
import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.*;
import java.util.*;
import io.kurumi.ntt.ui.ext.*;
import com.pengrad.telegrambot.request.*;
import io.kurumi.ntt.ui.request.*;

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

          

                AbsResuest resp = ProcessIndex.processUpdate(update);

                if (resp != null) resp.exec();
               
          
        }

        return CONFIRMED_UPDATES_ALL;

    }

}
