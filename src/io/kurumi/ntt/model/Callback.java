package io.kurumi.ntt.model;

import cn.hutool.core.util.*;
import com.pengrad.telegrambot.model.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.model.request.*;

public class Callback extends Msg {

    private CallbackQuery query;
	
	public String[] params;

    public Callback(CallbackQuery query) {

        this(Launcher.INSTANCE, query);

    }
	
    public Callback(Fragment fragment, CallbackQuery query) {

        super(fragment, query.message());

        this.query = query;

        this.params = query.data().contains(",") ? query.data().split(",") : new String[] { query.data() };

    }

    public void confirm() {

        answer().exec();

    }

    public void text(String[] text) {

        answer().text(ArrayUtil.join(text, "\n")).exec();

    }

    public void text(String text) {

        answer().cacheTime(0).text(text).exec();

    }

    public void alert(String[] alert) {

        answer().alert(ArrayUtil.join(alert, "\n")).exec();

    }


    public void alert(String alert) {

        answer().alert(alert).exec();

    }

    public void url(String url) {

        answer().url(url).exec();

    }

    public AnswerCallback answer() {

        return new AnswerCallback(fragment, query.id());

    }

}
