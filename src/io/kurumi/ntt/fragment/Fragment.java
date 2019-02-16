package io.kurumi.ntt.fragment;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.InlineQuery;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.response.BaseResponse;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.CData;
import io.kurumi.ntt.utils.ThreadPool;
import io.kurumi.ntt.model.Query;

public class Fragment {

    public TelegramBot bot;

    public boolean onMsg(UserData user, Msg msg) { return false; }

    public boolean onPoiMsg(UserData user, Msg msg , CData point) { return false; }

    public boolean onPrivMsg(UserData user, Msg msg) { return false; }

    public boolean onPoiPrivMsg(UserData user, Msg msg, CData point) { return false; }

    public boolean onGroupMsg(UserData user, Msg msg, boolean superGroup) { return false; }

    public boolean onPoiGroupMsg(UserData user, Msg msg, CData point, boolean superGroup) { return false; }

    public boolean onChanPost(UserData user, Msg msg) { return false; }

    public boolean onCallback(UserData user, Callback callback) { return false; }

    public boolean onPoiCallback(UserData user, Callback callback, CData point) { return false; }

    public boolean onQuery(UserData user, Query inlineQuery) { return false; }



}
