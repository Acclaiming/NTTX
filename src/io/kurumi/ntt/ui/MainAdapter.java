package io.kurumi.ntt.ui;

import cn.hutool.log.*;
import com.pengrad.telegrambot.*;
import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.*;
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

        try {

            for (Update update : updates) {

                ProcessIndex.processUpdate(update);

            }

            return CONFIRMED_UPDATES_ALL;

        } catch (Exception e) {

            log.error(e, "处理更新失败");

            return CONFIRMED_UPDATES_NONE;

        }




    }

}
