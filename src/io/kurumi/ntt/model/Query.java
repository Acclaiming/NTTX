package io.kurumi.ntt.model;

import com.pengrad.telegrambot.model.InlineQuery;
import com.pengrad.telegrambot.model.request.InlineQueryResult;
import com.pengrad.telegrambot.model.request.InlineQueryResultArticle;
import com.pengrad.telegrambot.model.request.InlineQueryResultDocument;
import com.pengrad.telegrambot.model.request.InputMessageContent;
import com.pengrad.telegrambot.model.request.InputTextMessageContent;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.AnswerInlineQuery;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.response.GetFileResponse;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.request.ButtonMarkup;
import io.kurumi.ntt.utils.BotLog;
import java.util.LinkedList;

public class Query {

    public Fragment fragment;
    public InlineQuery query;
    public LinkedList<InlineQueryResult> results = new LinkedList<>();

	public String text;
	
    public Query(Fragment fragment, InlineQuery query) {
        this.fragment = fragment;
        this.query = query;
		this.text = query.query();
    }
	
	
    public Query article(String title, String content,ParseMode parseMode,ButtonMarkup buttons) {

		InputTextMessageContent inputText = new InputTextMessageContent(content);
		
		if (parseMode != null) inputText.parseMode(parseMode);
		
        InlineQueryResultArticle result = new InlineQueryResultArticle(query.id(), title,inputText);
		
		if (buttons != null) {
			
			result.replyMarkup(buttons.markup());
			
		}
		
        results.add(result);

        return this;

    }

    public Query fileId(String fileName, String fileId) {

        GetFileResponse resp = fragment.bot().execute(new GetFile(fileId));

        if (!resp.isOk()) {

            BotLog.warnWithStack("没有那样的fileId对应的文件 : " + fileId);

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

		results.clear();
		
        return new AnswerInlineQuery(query.id(), results.toArray(new InlineQueryResult[results.size()]));

    }


}
