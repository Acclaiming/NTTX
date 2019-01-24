package io.kurumi.ntt.ui;

import cn.hutool.log.*;
import com.pengrad.telegrambot.*;
import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.*;
import java.util.*;
import io.kurumi.ntt.ui.ext.*;
import com.pengrad.telegrambot.request.*;

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

            try {

                ProcessIndex.processUpdate(update);

            } catch (Exception e) {

                log.error(e, "处理更新失败");

                for (UserData userData : Constants.data.getUsers()) {

                    if (userData.isAdmin && userData.chat != null) {

                        StringBuilder err = new StringBuilder();

                        err.append("Bot出错 : ");

                        err.append("\n更新 : " + update);

                        Throwable cause = e;

                        while (cause != null) {

                            err.append("\n\n错误 : " + cause.getClass().getName());

                            err.append("\n\n" + cause.getMessage());

                            for (StackTraceElement stack : cause.getStackTrace())  {

                                err.append("\nat : " + stack.toString());
                            }

                            cause = cause.getCause();

                        }

                        new MsgExt.Send(userData.chat, err.toString()).send();

                    }

                }

                return CONFIRMED_UPDATES_NONE;

            }

        }

        return CONFIRMED_UPDATES_ALL;






    }

}
