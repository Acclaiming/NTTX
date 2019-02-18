package io.kurumi.ntt.fragment;

import com.pengrad.telegrambot.TelegramBot;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.Query;
import io.kurumi.ntt.utils.CData;

public class Fragment {

    public BotFragment origin;

    public TelegramBot bot() {
        return origin.bot();
    }

    public boolean onMsg(UserData user, Msg msg) {
        return false;
    }

    public boolean onPoiMsg(UserData user, Msg msg, CData point) {
        return false;
    }

    public boolean onPrivMsg(UserData user, Msg msg) {
        return false;
    }

    public boolean onPoiPrivMsg(UserData user, Msg msg, CData point) {
        return false;
    }

    public boolean onGroupMsg(UserData user, Msg msg, boolean superGroup) {
        return false;
    }

    public boolean onPoiGroupMsg(UserData user, Msg msg, CData point, boolean superGroup) {
        return false;
    }

    public boolean onChanPost(UserData user, Msg msg) {
        return false;
    }

    public boolean onCallback(UserData user, Callback callback) {
        return false;
    }

    public boolean onPoiCallback(UserData user, Callback callback, CData point) {
        return false;
    }

    public boolean onQuery(UserData user, Query inlineQuery) {
        return false;
    }

    public CData cdata(String point) {

        CData data = new CData();

        data.setPoint(point);

        return data;

    }

    public CData cdata(String point, String index) {

        CData data = cdata(point);

        data.setindex(index);

        return data;

    }

}
