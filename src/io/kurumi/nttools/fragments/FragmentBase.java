package io.kurumi.nttools.fragments;

import com.pengrad.telegrambot.model.InlineQuery;
import io.kurumi.nttools.model.Callback;
import io.kurumi.nttools.model.Msg;
import io.kurumi.nttools.twitter.TwiAccount;
import io.kurumi.nttools.utils.CData;
import io.kurumi.nttools.utils.UserData;

public abstract class FragmentBase {
    
    public void processPrivateMessage(UserData user, Msg msg) {}
    public void processGroupMessage(UserData user, Msg msg) {}
    public void processChannelPost(UserData user, Msg msg) {}
    public void processCallbackQuery(UserData user, Callback callback) {}
    public void processInlineQuery(UserData user, InlineQuery inlineQuery) {}
    public void processChosenInlineQueryResult(UserData user, InlineQuery inlineQuery) {}

    
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

    public CData cdata(String point, UserData userData, TwiAccount account) {

        CData data = cdata(point);

        data.setUser(userData, account);

        return data;

    }

    public CData cdata(String point, String index, UserData userData, TwiAccount account) {

        CData data = cdata(point, index);

        data.setUser(userData, account);

        return data;

    }
    
}
