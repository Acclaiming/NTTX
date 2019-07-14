package io.kurumi.ntt.model;

import cn.hutool.core.util.ArrayUtil;
import com.pengrad.telegrambot.model.CallbackQuery;
import io.kurumi.ntt.Launcher;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.request.AnswerCallback;
import io.kurumi.ntt.db.UserData;

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

        answer().exec();

    }

    public void text(String... text) {

        answer().text(ArrayUtil.join(text, "\n")).exec();

    }

    public void text(String text) {

        answer().cacheTime(0).text(text).exec();

    }

    public void alert(String... alert) {

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
