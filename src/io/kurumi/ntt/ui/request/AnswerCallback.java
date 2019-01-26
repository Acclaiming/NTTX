package io.kurumi.ntt.ui.request;

import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.request.*;
import io.kurumi.ntt.*;

public class AnswerCallback {
    
    private AnswerCallbackQuery answer;

    public AnswerCallback(CallbackQuery query) {

        this(query.id());

    }

    public AnswerCallback(String id) {

        answer = new AnswerCallbackQuery(id);

    }

    public AnswerCallback text(String text) {

        answer.text(text);

        return this;

    }

    public AnswerCallback alert(String text) {

        text(text);

        answer.showAlert(true);

        return this;

    }

    public AnswerCallback url(String url) {

        answer.url(url);

        return this;

    }

    public AnswerCallback cacheTime(int sec) {

        answer.cacheTime(sec);

        return this;

    }

    public void exec() {

        Constants.bot.execute(answer);

    }
    
}
