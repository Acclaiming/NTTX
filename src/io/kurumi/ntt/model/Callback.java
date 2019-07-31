package io.kurumi.ntt.model;

import cn.hutool.core.util.ArrayUtil;
import com.pengrad.telegrambot.model.CallbackQuery;
import io.kurumi.ntt.Launcher;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.request.AnswerCallback;
import io.kurumi.ntt.db.UserData;
import com.pengrad.telegrambot.request.*;

public class Callback extends Msg {

    public String[] params;
    public CallbackQuery query;

    public Callback(CallbackQuery query) {

        this(Launcher.INSTANCE, query);

    }

    public Callback(Fragment fragment, CallbackQuery query) {

        super(fragment, query.message());

        this.query = query;

        this.params = query.data().contains(",") ? query.data().split(",") : new String[]{query.data()};

    }

    @Override
    public UserData from() {

        return UserData.get(query.from());

    }

    public String inlineMessgeId() {

        return query.inlineMessageId();

    }

    public void confirm() {

        fragment.executeAsync(update, answer());

    }

    public void text(String... text) {

        fragment.executeAsync(update, answer().text(ArrayUtil.join(text, "\n")));

    }

    public void alert(String... alert) {

        fragment.executeAsync(update, answer().text(ArrayUtil.join(alert, "\n")).showAlert(true));

    }

    public void payload(String... text) {

        url("https://t.me/" + fragment.origin.me.username() + "?start=" + ArrayUtil.join(text, fragment.PAYLOAD_SPLIT));

    }

    public void url(String url) {

        fragment.executeAsync(update, answer().url(url));

    }

    public AnswerCallbackQuery answer() {

        return new AnswerCallbackQuery(query.id());

    }

}
