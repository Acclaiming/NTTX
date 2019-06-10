package io.kurumi.ntt.fragment.abs;

import cn.hutool.core.util.ArrayUtil;
import com.pengrad.telegrambot.model.CallbackQuery;
import io.kurumi.ntt.Launcher;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.abs.request.AnswerCallback;

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
