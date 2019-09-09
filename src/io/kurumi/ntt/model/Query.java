package io.kurumi.ntt.model;

import cn.hutool.core.util.StrUtil;
import com.pengrad.telegrambot.model.InlineQuery;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.AnswerInlineQuery;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.response.GetFileResponse;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.request.ButtonMarkup;

import java.util.LinkedList;

public class Query {

    public Update update;
    public Fragment fragment;
    public InlineQuery query;
    public LinkedList<InlineQueryResult> results = new LinkedList<>();

    public String text;

    public Query(Fragment fragment, InlineQuery query) {
        this.fragment = fragment;
        this.query = query;
        this.text = query.query();
    }

    public boolean startsWith(String prefix) {

        return text != null && text.startsWith(prefix + " ") && !StrUtil.isEmpty(text.substring(prefix.length()));

    }

    public String queryString() {

        return StrUtil.subAfter(text, " ", false).trim();

    }

    public Query article(String title, String content, ParseMode parseMode, ButtonMarkup buttons) {

        InputTextMessageContent inputText = new InputTextMessageContent(content);

        if (parseMode != null) inputText.parseMode(parseMode);

        InlineQueryResultArticle result = new InlineQueryResultArticle(query.id(), title, inputText);

        if (buttons != null) {

            result.replyMarkup(buttons.markup());

        }

        results.add(result);

        return this;

    }

    public Query sticker(String fileId) {

        InlineQueryResultCachedSticker result = new InlineQueryResultCachedSticker(fileId, fileId);

        results.add(result);

        return this;

    }

    public Query fileId(String fileName, String fileId) {

        GetFileResponse resp = fragment.bot().execute(new GetFile(fileId));

        if (!resp.isOk()) {

            return this;

        }

        return fileUrl(fileName, fragment.bot().getFullFilePath(resp.file()));

    }

    public Query fileUrl(String fileName, String url) {

        String mimeType = "application/octet-stream";

        // TODO

        InlineQueryResultDocument result = new InlineQueryResultDocument(query.id(), url, fileName, mimeType);

        results.add(result);

        return this;

    }

    public AnswerInlineQuery reply() {

        AnswerInlineQuery answer = new AnswerInlineQuery(query.id(), results.toArray(new InlineQueryResult[results.size()]));

        results.clear();

        return answer;

    }


}
